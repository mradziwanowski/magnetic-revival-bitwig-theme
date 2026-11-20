package com.graphitelight.inkguard;

import bitwig.obf.iok; // immutable ARGB colour value
import com.bitwig.graphics.BrJ; // bitmap/icon surface with width (UNj) and height (IwV)
import com.bitwig.graphics.FontExtents;
import com.bitwig.graphics.render_commands.JXO; // render command recorder
import com.bitwig.graphics.render_commands.Uev; // gradient pattern builder
import com.bitwig.graphics.render_commands.gJi; // cloneable graphics state (clip MRY/Hnt/FzB/IwV)

import java.util.Arrays;
import java.util.HashMap;

/**
 * Surface-aware ink guard logic (exact port of java/guard/GuardTpl.java to BML).
 */
final class InkGuardLogic {
    private static final double G12_FLOOR = 3.0;
    private static final double G12_TARGET = 4.7;
    private static final int G12_CELL = 64;
    private static final int G12_CELLS = 64;

    private static final HashMap<Long, Object> CACHE = new HashMap<>();

    static final class RecorderState {
        double[] s;
        int[] c;
        int[][] grid;
        int[] gridN;
        int n;
        int kind;
        double x0, y0, x1, y1;
    }

    static final class GradState {
        int min;
        int max;
        int last;
        int stops;
    }

    private InkGuardLogic() {}

    private static RecorderState rec(JXO self) {
        RecorderState st = (RecorderState) Fields.RECORDER_STATE.get(self);
        if (st == null) {
            st = new RecorderState();
            Fields.RECORDER_STATE.set(self, st);
        }
        return st;
    }

    private static GradState grad(Uev u, boolean create) {
        GradState g = (GradState) Fields.GRAD_STATE.get(u);
        if (g == null && create) {
            g = new GradState();
            Fields.GRAD_STATE.set(u, g);
        }
        return g;
    }

    /* ---------- pure colour math ---------- */

    static double lin(int v) {
        double c = v / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    static double lum(int rgb) {
        return 0.2126 * lin((rgb >> 16) & 255)
                + 0.7152 * lin((rgb >> 8) & 255)
                + 0.0722 * lin(rgb & 255);
    }

    static double contrast(int a, int b) {
        double la = lum(a), lb = lum(b);
        return la > lb ? (la + 0.05) / (lb + 0.05) : (lb + 0.05) / (la + 0.05);
    }

    static int blend(int fg, int alpha, int bg) {
        int r = (((fg >> 16) & 255) * alpha + ((bg >> 16) & 255) * (255 - alpha) + 127) / 255;
        int g = (((fg >> 8) & 255) * alpha + ((bg >> 8) & 255) * (255 - alpha) + 127) / 255;
        int b = ((fg & 255) * alpha + (bg & 255) * (255 - alpha) + 127) / 255;
        return (r << 16) | (g << 8) | b;
    }

    static int mix(int rgb, int toward, double t) {
        int r = (int) Math.round(((rgb >> 16) & 255) + (toward - ((rgb >> 16) & 255)) * t);
        int g = (int) Math.round(((rgb >> 8) & 255) + (toward - ((rgb >> 8) & 255)) * t);
        int b = (int) Math.round((rgb & 255) + (toward - (rgb & 255)) * t);
        return (r << 16) | (g << 8) | b;
    }

    static int adjustArgb(int ink, int bg) {
        int alpha = ink >>> 24;
        if (alpha < 24) return ink;
        bg &= 0xffffff;
        int eff = blend(ink & 0xffffff, alpha, bg);
        double want = alpha < 128 ? G12_FLOOR : G12_TARGET;
        if (contrast(eff, bg) >= want) return ink;
        double lb = lum(bg);
        int toward = (lb + 0.05) / 0.05 >= 1.05 / (lb + 0.05) ? 0 : 255;
        double lo = 0, hi = 1;
        for (int i = 0; i < 12; i++) {
            double mid = (lo + hi) / 2;
            if (contrast(mix(eff, toward, mid), bg) >= want) hi = mid; else lo = mid;
        }
        return 0xff000000 | mix(eff, toward, hi);
    }

    static int adjustIcon(int ink, int bg) {
        int alpha = ink >>> 24;
        if (alpha < 128) return ink; // as GuardTpl; the 0x7f icons were the missing dsi graphiteIconContrast
        bg &= 0xffffff;
        int eff = blend(ink & 0xffffff, alpha, bg);
        if (contrast(eff, bg) >= G12_FLOOR) return ink;
        double lb = lum(bg);
        int toward = (lb + 0.05) / 0.05 >= 1.05 / (lb + 0.05) ? 0 : 255;
        double lo = 0, hi = 1;
        for (int i = 0; i < 12; i++) {
            double mid = (lo + hi) / 2;
            if (contrast(mix(eff, toward, mid), bg) >= G12_FLOOR + 0.2) hi = mid; else lo = mid;
        }
        return 0xff000000 | mix(eff, toward, hi);
    }

    /* ---------- graphics state helpers ---------- */

    private static double sx(gJi s) {
        double v = (double) Fields.SX.get(s);
        return v == 0 ? 1.0 : v;
    }

    private static double sy(gJi s) {
        double v = (double) Fields.SY.get(s);
        return v == 0 ? 1.0 : v;
    }

    private static int clampByte(double v) {
        int i = (int) Math.round(v * 255.0);
        return i < 0 ? 0 : i > 255 ? 255 : i;
    }

    static void translate(JXO self, double dx, double dy) {
        try {
            gJi s = self.hUR(); // current graphics state
            if (s == null) return;
            Fields.TX.set(s, (double) Fields.TX.get(s) + dx * sx(s));
            Fields.TY.set(s, (double) Fields.TY.get(s) + dy * sy(s));
        } catch (Throwable ignored) {}
    }

    static void scale(JXO self, double x, double y) {
        try {
            gJi s = self.hUR(); // current graphics state
            if (s == null) return;
            Fields.SX.set(s, sx(s) * x);
            Fields.SY.set(s, sy(s) * y);
        } catch (Throwable ignored) {}
    }

    static void rotate(JXO self, double a) {
        try {
            gJi s = self.hUR(); // current graphics state
            if (s == null) return;
            Fields.ROT.set(s, (double) Fields.ROT.get(s) + a);
        } catch (Throwable ignored) {}
    }

    static void color(JXO self, double r, double g, double b, double a) {
        try {
            gJi s = self.hUR(); // current graphics state
            if (s == null) return;
            int argb = (clampByte(a) << 24) | (clampByte(r) << 16) | (clampByte(g) << 8) | clampByte(b);
            Fields.ARGB.set(s, argb);
            Fields.SOLID.set(s, true);
        } catch (Throwable ignored) {}
    }

    static void rule(JXO self, Object rule) {
        try {
            gJi s = self.hUR(); // current graphics state
            if (s != null) {
                Fields.EVEN_ODD.set(s, rule instanceof Enum<?> e && e.ordinal() == 1);
            }
        } catch (Throwable ignored) {}
    }

    static void pattern(JXO self, Object pattern) {
        try {
            gJi s = self.hUR(); // current graphics state
            if (s == null) return;
            Fields.SOLID.set(s, false);
            if (!(pattern instanceof Uev u)) return;
            GradState gs = grad(u, false);
            if (gs == null || gs.stops == 0) return;
            int mn = gs.min, mx = gs.max;
            for (int sh = 0; sh <= 16; sh += 8) {
                if (((mx >> sh) & 255) - ((mn >> sh) & 255) > 40) return;
            }
            if ((gs.last >>> 24) < 235) return;
            int r = (((mn >> 16) & 255) + ((mx >> 16) & 255)) / 2;
            int g = (((mn >> 8) & 255) + ((mx >> 8) & 255)) / 2;
            int b = ((mn & 255) + (mx & 255)) / 2;
            Fields.ARGB.set(s, 0xff000000 | (r << 16) | (g << 8) | b);
            Fields.SOLID.set(s, true);
        } catch (Throwable ignored) {}
    }

    static void stop(Uev self, iok c) {
        try {
            if (c == null) return;
            GradState gs = grad(self, true);
            int v = c.qoU(); // packed ARGB
            int mn = gs.stops == 0 ? v : gs.min;
            int mx = gs.stops == 0 ? v : gs.max;
            int lo = 0, hi = 0;
            for (int sh = 0; sh <= 16; sh += 8) {
                int ch = (v >> sh) & 255;
                lo |= Math.min(ch, (mn >> sh) & 255) << sh;
                hi |= Math.max(ch, (mx >> sh) & 255) << sh;
            }
            gs.min = lo;
            gs.max = hi;
            gs.last = v;
            gs.stops++;
        } catch (Throwable ignored) {}
    }

    /* ---------- pending path bbox ---------- */

    private static void extend(RecorderState st, gJi s, double x, double y, int kind) {
        double dx = (double) Fields.TX.get(s) + x * sx(s);
        double dy = (double) Fields.TY.get(s) + y * sy(s);
        if ((double) Fields.ROT.get(s) != 0) { st.kind = 3; return; }
        if (st.kind == 0) {
            st.x0 = dx; st.x1 = dx; st.y0 = dy; st.y1 = dy;
            st.kind = kind;
        } else {
            if (dx < st.x0) st.x0 = dx;
            if (dx > st.x1) st.x1 = dx;
            if (dy < st.y0) st.y0 = dy;
            if (dy > st.y1) st.y1 = dy;
            if (st.kind < kind) st.kind = kind;
        }
    }

    static void rect(JXO self, double x, double y, double w, double h) {
        try {
            gJi s = self.hUR();
            if (s == null) return;
            RecorderState st = rec(self);
            int kind = st.kind == 0 ? 1 : 2;
            extend(st, s, x, y, kind);
            extend(st, s, x + w, y + h, kind);
        } catch (Throwable ignored) {}
    }

    static void point(JXO self, double x, double y) {
        try {
            gJi s = self.hUR();
            if (s != null) extend(rec(self), s, x, y, 2);
        } catch (Throwable ignored) {}
    }

    static void arc(JXO self, double cx, double cy, double r) {
        try {
            gJi s = self.hUR();
            if (s == null) return;
            RecorderState st = rec(self);
            extend(st, s, cx - r, cy - r, 2);
            extend(st, s, cx + r, cy + r, 2);
        } catch (Throwable ignored) {}
    }

    static void curve(JXO self, double x1, double y1, double x2, double y2, double x3, double y3) {
        try {
            gJi s = self.hUR();
            if (s == null) return;
            RecorderState st = rec(self);
            extend(st, s, x1, y1, 2);
            extend(st, s, x2, y2, 2);
            extend(st, s, x3, y3, 2);
        } catch (Throwable ignored) {}
    }

    static void pathClear(JXO self) {
        try {
            RecorderState st = (RecorderState) Fields.RECORDER_STATE.get(self);
            if (st != null) st.kind = 0;
        } catch (Throwable ignored) {}
    }

    /* ---------- surface spatial index ---------- */

    private static int cell(double v) {
        int c = (int) Math.floor(v / G12_CELL);
        return c < 0 ? 0 : c >= G12_CELLS ? G12_CELLS - 1 : c;
    }

    private static void index(RecorderState st, int n) {
        if (st.grid == null) {
            st.grid = new int[G12_CELLS * G12_CELLS][];
            st.gridN = new int[G12_CELLS * G12_CELLS];
        }
        int o = 4 * n;
        int cx0 = cell(st.s[o]), cy0 = cell(st.s[o + 1]);
        int cx1 = cell(st.s[o + 2] - 0.001), cy1 = cell(st.s[o + 3] - 0.001);
        for (int cy = cy0; cy <= cy1; cy++) {
            for (int cx = cx0; cx <= cx1; cx++) {
                int k = cy * G12_CELLS + cx;
                int[] l = st.grid[k];
                int m = st.gridN[k];
                if (l == null) { l = new int[8]; st.grid[k] = l; }
                else if (m == l.length) { l = Arrays.copyOf(l, 2 * m); st.grid[k] = l; }
                l[m] = n;
                st.gridN[k] = m + 1;
            }
        }
    }

    private static void record(RecorderState st, gJi s, double x0, double y0, double x1, double y1, int argb) {
        double scx = sx(s), scy = sy(s);
        double tx = (double) Fields.TX.get(s), ty = (double) Fields.TY.get(s);
        // gJi clip bounds: MRY=x, Hnt=y, FzB=w, IwV=h (non-public; opened by BML)
        double cx0 = tx + s.MRY * scx, cy0 = ty + s.Hnt * scy;
        double cx1 = cx0 + s.FzB * scx, cy1 = cy0 + s.IwV * scy;
        if (x0 < cx0) x0 = cx0;
        if (y0 < cy0) y0 = cy0;
        if (x1 > cx1) x1 = cx1;
        if (y1 > cy1) y1 = cy1;
        if (x1 - x0 < 1 || y1 - y0 < 1) return;
        int n = st.n;
        if (st.s == null) { st.s = new double[4 * 128]; st.c = new int[128]; }
        if (n == st.c.length) {
            if (n >= 8192) {
                int keep = n / 2;
                System.arraycopy(st.s, 4 * (n - keep), st.s, 0, 4 * keep);
                System.arraycopy(st.c, n - keep, st.c, 0, keep);
                n = keep;
                if (st.gridN != null) Arrays.fill(st.gridN, 0);
                for (int i = 0; i < n; i++) { st.n = i; index(st, i); }
            } else {
                st.s = Arrays.copyOf(st.s, 8 * n);
                st.c = Arrays.copyOf(st.c, 2 * n);
            }
        }
        int o = 4 * n;
        st.s[o] = x0; st.s[o + 1] = y0; st.s[o + 2] = x1; st.s[o + 3] = y1;
        st.c[n] = argb;
        index(st, n);
        st.n = n + 1;
    }

    private static int lookup(RecorderState st, double px, double py) {
        double[] r = st.s;
        if (r == null || st.grid == null) return 0;
        int k = cell(py) * G12_CELLS + cell(px);
        int[] l = st.grid[k];
        if (l == null) return 0;
        for (int j = st.gridN[k] - 1; j >= 0; j--) {
            int i = l[j];
            if (i >= st.n) continue;
            int o = 4 * i;
            if (px >= r[o] && px < r[o + 2] && py >= r[o + 1] && py < r[o + 3]) {
                return st.c[i];
            }
        }
        return 0;
    }

    private static void surface(RecorderState st, gJi s, double x0, double y0, double x1, double y1,
                                int argb, boolean solid, boolean freeForm) {
        if (!solid) argb = 1;
        else {
            int alpha = argb >>> 24;
            if (alpha < 20) return;
            if (freeForm && (x1 - x0 > 240 || y1 - y0 > 120)) argb = 3;
            else if (alpha < 235) {
                int under = lookup(st, (x0 + x1) / 2, (y0 + y1) / 2);
                argb = (under >>> 24) == 0 ? 4 : 0xff000000 | blend(argb & 0xffffff, alpha, under & 0xffffff);
            } else argb |= 0xff000000;
        }
        record(st, s, x0, y0, x1, y1, argb);
    }

    static void fill(JXO self, boolean clear) {
        try {
            RecorderState st = rec(self);
            int kind = st.kind;
            if (clear) st.kind = 0;
            if (kind == 0 || kind == 3) return;
            gJi s = self.hUR();
            if (s == null || (boolean) Fields.EVEN_ODD.get(s)) return;
            surface(st, s, st.x0, st.y0, st.x1, st.y1,
                    (int) Fields.ARGB.get(s), (boolean) Fields.SOLID.get(s), kind == 2);
        } catch (Throwable ignored) {}
    }

    static void fillRect(JXO self, double x, double y, double w, double h, int argb) {
        try {
            gJi s = self.hUR();
            if (s == null || (double) Fields.ROT.get(s) != 0) return;
            double scx = sx(s), scy = sy(s);
            double x0 = (double) Fields.TX.get(s) + x * scx;
            double y0 = (double) Fields.TY.get(s) + y * scy;
            surface(rec(self), s, x0, y0, x0 + w * scx, y0 + h * scy, argb, true, false);
        } catch (Throwable ignored) {}
    }

    static void image(JXO self, Object img, double x, double y) {
        try {
            gJi s = self.hUR();
            if (s == null || !(img instanceof BrJ b)) return;
            double scx = sx(s), scy = sy(s);
            double x0 = (double) Fields.TX.get(s) + x * scx;
            double y0 = (double) Fields.TY.get(s) + y * scy;
            // BrJ.UNj()=width, BrJ.IwV()=height
            record(rec(self), s, x0, y0, x0 + b.UNj() * scx, y0 + b.IwV() * scy, 2);
        } catch (Throwable ignored) {}
    }

    static void icon(JXO self, Object img, double x, double y) {
        try {
            gJi s = self.hUR();
            if (s == null || !(boolean) Fields.SOLID.get(s) || (double) Fields.ROT.get(s) != 0) return;
            RecorderState st = (RecorderState) Fields.RECORDER_STATE.get(self);
            if (st == null) return;
            double w = 16, h = 16;
            if (img instanceof BrJ b) { w = b.UNj(); h = b.IwV(); }
            double px = (double) Fields.TX.get(s) + (x + w / 2) * sx(s);
            double py = (double) Fields.TY.get(s) + (y + h / 2) * sy(s);
            int bg = lookup(st, px, py);
            if ((bg >>> 24) == 0) return;
            int ink = (int) Fields.ARGB.get(s);
            int out = adjustIcon(ink, bg);
            if (out == ink) return;
            // JXO.FzB(r,g,b,a) sets current RGBA paint colour
            self.FzB(((out >> 16) & 255) / 255.0, ((out >> 8) & 255) / 255.0, (out & 255) / 255.0, (out >>> 24) / 255.0);
        } catch (Throwable ignored) {}
    }

    static iok ink(JXO self, double x, double y, String text, iok color) {
        try {
            if (color == null || text == null) return color;
            RecorderState st = (RecorderState) Fields.RECORDER_STATE.get(self);
            if (st == null) return color;
            gJi s = self.hUR();
            if (s == null || (double) Fields.ROT.get(s) != 0) return color;
            int ink = color.qoU(); // packed ARGB
            FontExtents fe = self.iHb(); // current font extents
            double ascent = fe == null ? 12.0 : fe.cfM(); // ascent
            double px = (double) Fields.TX.get(s) + (x + ascent * 0.6) * sx(s);
            double py = (double) Fields.TY.get(s) + (y - ascent * 0.35) * sy(s);
            int bg = lookup(st, px, py);
            if ((bg >>> 24) == 0) return color;
            Long key = Long.valueOf(((long) ink << 32) | (bg & 0xffffffffL));
            Object hit;
            synchronized (CACHE) {
                hit = CACHE.get(key);
                if (hit == null) {
                    int out = adjustArgb(ink, bg);
                    hit = out == ink ? Boolean.FALSE : new iok(out);
                    if (CACHE.size() > 8192) CACHE.clear();
                    CACHE.put(key, hit);
                }
            }
            return hit instanceof iok c ? c : color;
        } catch (Throwable ignored) {
            return color;
        }
    }
}
