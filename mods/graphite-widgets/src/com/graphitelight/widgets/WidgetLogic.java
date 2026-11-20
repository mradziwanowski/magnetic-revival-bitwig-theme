package com.graphitelight.widgets;

import bitwig.obf.Fot; // device panel model
import bitwig.obf.Hfk; // panel brightness enum (MRY = DARKER)
import bitwig.obf.R1Z; // device descriptor (qoU() = UUID)
import bitwig.obf.aJc; // Inspector panel container (6.1.8: aJq)
import bitwig.obf.aOV; // EQ curve widget (6.1.8: aPl)
import bitwig.obf.aog; // device-header power button (6.1.8: aor)
import bitwig.obf.cCq; // device chain row owner (6.1.8: cDy)
import bitwig.obf.dpN; // application context (TVZ() = singleton, cfM(Runnable, long) = UI timer)
import bitwig.obf.dqT; // SVG icon widget (6.1.8: dsi)
import bitwig.obf.dve; // base widget (RbC() = parent, TMF(Class) = ancestor, Sxo() = w, yiX() = h, jbi() = invalidate)
import bitwig.obf.dzv; // checkbox/toggle button (6.1.8: dAR)
import bitwig.obf.eeY; // named colour role
import bitwig.obf.efd; // colour role mode enum (cfM = ABSOLUTE)
import bitwig.obf.eff; // theme colour palette
import bitwig.obf.iBH; // device group layout model
import bitwig.obf.iok; // packed ARGB colour
import bitwig.obf.zaq; // panel border style enum (MRY = FILLED)
import com.bitwig.flt.widget.core.device.L1F; // device outer widget (6.1.8: u2H)
import com.bitwig.flt.widget.core.device.g2L; // mixer device row / header (6.1.8: jF)
import com.bitwig.graphics.SYo; // graphics context (6.1.8: sz2)

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/** Pure helper logic for graphite-widgets (including the v07n17 DeviceIndicators port). */
final class WidgetLogic {
    private static final String EQ_PLUS_UUID = "e4815188-ba6f-4d14-bcfc-2dcb8f778ccb";

    // Mixer row -> {WeakReference<aOV>, Boolean lastEnabled}
    private static final Map<Object, Object[]> CURVE_ROWS = new WeakHashMap<>();

    private WidgetLogic() {}

    static eeY mainBodyRole(eeY original, iBH model, eff palette) {
        if (original == null || model == null || palette == null
                || model.hUR() != zaq.MRY || model.LaE() != Hfk.MRY) return original;
        if (!(model.iCS() instanceof Fot panel)) return original;
        if (panel.E1b() != model || !"Main".equals(panel.UZr())) return original;
        eeY role = palette.cfM("Grey 3"); // #bcbcbc (Device Main Body)
        return role == null ? original : role;
    }

    static iok neutralSceneItem(iok original) {
        if (original == null) return null;
        if ((original.qoU() & 0xffffff) == 0x494949) {
            return new iok(0xffb0b0b0);
        }
        return original;
    }

    private static double channelLin(int v) {
        double c = v / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double wcag(iok a, iok b) {
        int ia = a.qoU(), ib = b.qoU();
        double la = 0.2126 * channelLin((ia >> 16) & 255) + 0.7152 * channelLin((ia >> 8) & 255) + 0.0722 * channelLin(ia & 255);
        double lb = 0.2126 * channelLin((ib >> 16) & 255) + 0.7152 * channelLin((ib >> 8) & 255) + 0.0722 * channelLin(ib & 255);
        return la > lb ? (la + 0.05) / (lb + 0.05) : (lb + 0.05) / (la + 0.05);
    }

    static eeY searchIconInk(eff palette) {
        eeY ink = palette.x3t();
        eeY field = palette.Scv();
        if (ink == null || field == null || ink.E3() != efd.cfM || field.E3() != efd.cfM) return ink;
        iok inkColour = ink.iHb();
        iok fieldColour = field.iHb();
        if (inkColour == null || fieldColour == null || !inkColour.iHb() || !fieldColour.iHb()) return ink;
        double before = wcag(inkColour, fieldColour);
        if (before >= 3.0) return ink;
        iok light = new iok(0xffe6e6e6);
        return wcag(light, fieldColour) > before ? eeY.cfM("Graphite Search Icon", light) : ink;
    }

    /**
     * v07n17 graphiteTraceContrast (aSD peak limiter trace, dNn display plot; bytecode-checked against
     * v07n17-618 aTc/dOM): any trace on a dark or unresolved background becomes #4dc8ff.
     */
    static iok traceContrast(iok original, dve widget) {
        if (original == null || widget == null) return original;
        iok bg = widget.YxM(); // resolved widget background colour
        if (bg == null || !bg.iHb() || bg.qoU() == 0xff000000 || bg.qoU() == 0xff181818
                || bg.qoU() == 0xff242424 || bg.MRY() <= 0.25f) {
            return new iok(0xff4dc8ff);
        }
        return original;
    }

    /** v07n17 graphiteCurveContrast (aSz Saturator curve): only dark curves on dark displays. */
    static iok curveContrast(iok original, dve widget) {
        if (original == null || widget == null) return original;
        iok bg = widget.YxM(); // resolved widget background colour
        if (bg != null && bg.iHb() && (bg.qoU() == 0xff242424 || bg.MRY() <= 0.25f)) {
            if (original.MRY() <= 0.35f) {
                return new iok(0xff4dc8ff);
            }
        }
        return original;
    }

    /* ---------- v07n17 DeviceIndicators (ported from 6.1.8 to 6.1.4 names) ---------- */

    private static void indicatorDisk(SYo g, double x, double y, double r, int argb) {
        g.nVd(); // newPath (6.1.8: FXY)
        g.Hnt(new iok(argb)); // setColor (6.1.8: ZxN)
        g.cfM(x, y, r, 0, 2 * Math.PI); // arc (6.1.8: VDr)
        g.wGc(); // fill (6.1.8: eTZ)
    }

    /**
     * v07n17 dsi.graphiteIconContrast = graphiteGeneralContrast (verbatim port of java/BatchLPaint.java,
     * bytecode-checked against v07n17-618 dsi). Applied to the icon ink before chainIndicator.
     * Only RGB (MRY/hbP/Grk/tp2) is read, so the input alpha does not matter.
     */
    static iok generalContrast(iok original, dve widget) {
        if (original == null || widget == null) return original;
        iok bg = widget.YxM(); // background colour
        dve p = widget;
        int hops = 0;
        while ((bg == null || !bg.iHb()) && p != null && hops < 8) {
            p = p.RbC(); // parent
            if (p != null) bg = p.YxM();
            hops++;
        }
        if (bg == null || !bg.iHb()) return original;
        int bgArgb = bg.qoU();
        float bgLum = bg.MRY();
        float fgLum = original.MRY();
        if (bgLum <= 0.25f || bgArgb == 0xff242424) {
            if (fgLum <= 0.35f) {
                return fgLum <= 0.18f ? new iok(0xffe6e6e6) : new iok(0xffa8a8a8);
            }
            return original;
        }
        if (bgLum >= 0.45f) {
            int r = original.hbP(), g = original.Grk(), b = original.tp2();
            if (g > 150 && b > 100 && r < 120) return new iok(0xff082a1e);
            if (r > 200 && g > 180 && b < 100) return new iok(0xff332c0d);
            if (r > 200 && g > 90 && g < 180 && b < 60) return new iok(0xff401e00);
            if (r > 200 && g < 80 && b < 80) return new iok(0xff33060d);
            if (g > 160 && r < 200 && b < 80) return new iok(0xff143300);
            if (b > 180 && g > 180 && r < 80) return new iok(0xff003638);
            if (b > 180 && r < 120) return new iok(0xff0a2247);
            if (r > 160 && b > 200 && g < 150) return new iok(0xff271633);
            float maxL = Math.max(bgLum, fgLum);
            float minL = Math.min(bgLum, fgLum);
            float contrast = (maxL + 0.05f) / (minL + 0.05f);
            if (contrast < 4.0f && fgLum >= 0.35f) {
                float factor = 0.30f;
                int dr = Math.round(r * factor), dg = Math.round(g * factor), db = Math.round(b * factor);
                return new iok(0xff000000 | (dr << 16) | (dg << 8) | db);
            }
        }
        return original;
    }

    /**
     * Icon tint as v07n17 dsi paint computes it: contrast, then chainIndicator, then the icon
     * opacity. The hook sees the ink after the opacity scale (dqT.Mlm, iok.cfM(F)), so a changed
     * colour is scaled again; an unchanged one is passed through as is.
     */
    static iok iconInk(iok scaledInk, dqT icon, SYo graphics) {
        if (scaledInk == null || icon == null) return scaledInk;
        iok adjusted = generalContrast(scaledInk, icon);
        iok out = chainIndicator(adjusted, scaledInk, icon, graphics);
        if (out != scaledInk && out != null) {
            double opacity = icon.Mlm(); // icon opacity (6.1.8: kDM)
            if (opacity < 1.0) out = out.cfM((float) opacity); // alpha * opacity (6.1.8: VDr(F))
        }
        return out;
    }

    static iok chainIndicator(iok adjusted, iok nativeInk, dqT icon, SYo graphics) {
        if (icon == null || nativeInk == null || graphics == null) return adjusted;
        String name = icon.QOl(); // icon file name (6.1.8: av)
        if (name == null || !name.startsWith("device_")) return adjusted;
        switch (name) {
            case "device_on.svg":
            case "device_processing.svg":
            case "device_off.svg":
            case "device_deactivated.svg":
            case "device_sleeping.svg":
                break;
            default:
                return adjusted;
        }
        dve parent = icon.RbC(); // parent widget (6.1.8: pxw)
        if (parent == null) return adjusted;
        dve owner = parent.RbC();
        Object mixerRow = icon.TMF(g2L.class); // findAncestor (6.1.8: IfS)
        // If this is a mixer row with an EQ+ miniature whose painted enabled state is stale, invalidate it.
        if (mixerRow instanceof g2L row) {
            Object[] state;
            synchronized (CURVE_ROWS) {
                state = CURVE_ROWS.get(row);
            }
            if (state != null && state[0] instanceof WeakReference<?> ref && ref.get() instanceof aOV curve) {
                Boolean enabled = row.LEd().wZs().iCS().getBoolValue(); // diq().VDT().cXa().getBoolValue()
                if (!enabled.equals(state[1])) {
                    state[1] = enabled;
                    curve.jbi(); // invalidate (6.1.8: Ypl)
                }
            }
        }
        if (!(mixerRow instanceof g2L) && !(owner instanceof cCq) && !(icon.TMF(aog.class) instanceof aog)) {
            return adjusted;
        }
        int rgb = nativeInk.qoU(); // packed ARGB (6.1.8: kiD)
        boolean lit = ((rgb >> 16) & 255) > (rgb & 255) + 20
                && !name.equals("device_off.svg") && !name.equals("device_deactivated.svg");
        double x = icon.Sxo() / 2.0, y = icon.yiX() / 2.0; // width/height (6.1.8: cZp/UyZ)
        double r = Math.min(x, y) - 1;
        if (r <= 2) return adjusted;
        graphics.qoU(); // save (6.1.8: kiD)
        indicatorDisk(graphics, x, y, r, 0xff363636);
        indicatorDisk(graphics, x, y, r - Math.max(1, r * 0.14), lit ? 0xffffbd27 : 0xffc6c6c6);
        graphics.ucQ(); // restore (6.1.8: b3w)
        return new iok(lit ? 0xff211c11 : 0xff555555);
    }

    static void mixerCurve(aOV curve, SYo graphics) {
        if (curve == null || graphics == null) return;
        if (!(curve.TMF(g2L.class) instanceof g2L row)) return;
        if (!(row.LEd().OzW() instanceof R1Z device)
                || !EQ_PLUS_UUID.equals(String.valueOf(device.qoU()))) return;
        boolean enabled = row.LEd().wZs().iCS().getBoolValue();
        synchronized (CURVE_ROWS) {
            Object[] state = CURVE_ROWS.get(row);
            if (state == null || !(state[0] instanceof WeakReference<?> ref) || ref.get() != curve) {
                state = new Object[] {new WeakReference<>(curve), null};
                CURVE_ROWS.put(row, state);
            }
            state[1] = enabled;
        }
        if (!enabled) {
            graphics.Hnt(new iok(0xffa6a6a6)); // grey EQ+ miniature in bypass
        }
    }

    static void deferHeaderPowerRepaint(dqT icon) {
        if (icon == null) return;
        String name = icon.QOl();
        if (name == null || !name.startsWith("device_") || !(icon.TMF(aog.class) instanceof aog)) return;
        WeakReference<dqT> ref = new WeakReference<>(icon);
        dpN.TVZ().cfM(() -> {
            dqT ic = ref.get();
            if (ic == null) return;
            dve widget = ic;
            for (int depth = 0; widget != null && depth < 16; depth++, widget = widget.RbC()) {
                widget.jbi(); // invalidate up to L1F device container
                if (widget instanceof L1F) break;
            }
        }, 50L);
    }

    static void inspectorIndicator(dzv button, SYo graphics, double size) {
        if (button == null || graphics == null) return;
        if (!(button.TMF(aJc.class) instanceof aJc)) return;
        boolean on = button.wuu(); // checked state (6.1.8: bAi)
        double x = size / 2.0, y = Math.floor((button.yiX() - size) / 2.0) + x;
        if (x <= 2) return;
        graphics.qoU(); // save
        indicatorDisk(graphics, x, y, x - 0.5, 0xffebebeb);
        indicatorDisk(graphics, x, y, x - Math.max(2.0, x * 0.14), 0xff383838);
        if (on) indicatorDisk(graphics, x, y, x - Math.max(2.5, x * 0.17), 0xffffbd27);
        else indicatorDisk(graphics, x, y, x - Math.max(2.5, x * 0.23), 0xff9b9b9b);
        graphics.ucQ(); // restore
    }
}
