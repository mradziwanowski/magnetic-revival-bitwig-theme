package com.graphitelight.widgets;

import bitwig.obf.ES; // device definition (6.1.8: XsW route)
import bitwig.obf.Fot; // device panel model (6.1.8: k3y)
import bitwig.obf.Hfk; // panel brightness enum
import bitwig.obf.aOl; // device group panel widget
import bitwig.obf.aOy; // enum/list chooser widget (6.1.8: aOL)
import bitwig.obf.aQG; // device panel paint router
import bitwig.obf.aoH; // outer chooser panel (6.1.8: aoV)
import bitwig.obf.cNP; // paint filter interface
import bitwig.obf.cNQ; // composite paint filter
import bitwig.obf.cNR; // alpha paint filter
import bitwig.obf.dBX; // popup chip button
import bitwig.obf.dBv; // button group chooser
import bitwig.obf.dCQ; // tint paint filter
import bitwig.obf.dND; // multi-value number field (6.1.8: dPc)
import bitwig.obf.dNN; // enum/value field (6.1.8: dPm)
import bitwig.obf.dfh; // panel model interface
import bitwig.obf.duY; // label widget (exact)
import bitwig.obf.duu; // font-bearing label base
import bitwig.obf.dvb; // label widget (exact)
import bitwig.obf.dve; // base widget
import bitwig.obf.eeG; // button group style
import bitwig.obf.eeM; // style box state
import bitwig.obf.eeY; // named colour role
import bitwig.obf.efu; // font style
import bitwig.obf.egC; // button style
import bitwig.obf.egp; // field style
import bitwig.obf.eff; // theme palette
import bitwig.obf.gvB; // device definition holder
import bitwig.obf.iBH; // device group layout model
import bitwig.obf.icl; // device panel model owner
import bitwig.obf.iok; // ARGB colour
import bitwig.obf.zaq; // panel border style enum
import com.bitwig.graphics.SYo;
import com.bitwig.graphics.WzZ;

/**
 * Verbatim ports of the v07n17 paint helpers that were transplanted into dut, dND, dNN, aoH, aEG
 * and aOy (sources: java/BatchMPaint.java, java/BatchM2Paint.java, java/ReadabilityPaintH.java,
 * java/ReadabilityPaintI.java, java/EqInspectorRevisionE.java). Names keep their batch suffix.
 * graphiteGeneralContrast is the identity in v07n17 for dNN/dut (neutralised by the ink guard build);
 * the dsi (6.1.4 dqT) icon copy stays active: see WidgetLogic.generalContrast.
 */
final class V07Paint {
    private V07Paint() {}

    static iok graphiteGeneralContrast(iok original, dve widget) {
        return original;
    }

    /* ---------- BatchM / M2: v07l CASE1/CASE2 rules on an explicit surface ---------- */

    static iok graphiteContrastOnM(iok original, iok bg) {
        if (original == null || bg == null || !bg.iHb()) return original;
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
            int r = original.hbP();
            int g = original.Grk();
            int b = original.tp2();
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
                int dr = Math.round(r * factor);
                int dg = Math.round(g * factor);
                int db = Math.round(b * factor);
                return new iok(0xff000000 | (dr << 16) | (dg << 8) | db);
            }
        }
        return original;
    }

    static double graphiteChannelM2(int value) {
        double s = value / 255.0;
        return s <= 0.04045 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
    }

    static double graphiteWcagM2(iok a, iok b) {
        double la = 0.2126 * graphiteChannelM2(a.hbP()) + 0.7152 * graphiteChannelM2(a.Grk()) + 0.0722 * graphiteChannelM2(a.tp2());
        double lb = 0.2126 * graphiteChannelM2(b.hbP()) + 0.7152 * graphiteChannelM2(b.Grk()) + 0.0722 * graphiteChannelM2(b.tp2());
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    static eeY graphiteMainBodyM2(eeY original, iBH model, eff palette) {
        if (original == null || model == null || palette == null
            || model.hUR() != zaq.MRY || model.LaE() != Hfk.MRY) return original;
        if (!(model.iCS() instanceof Fot)) return original;
        Fot panel = (Fot) model.iCS();
        if (panel.E1b() != model || !"Main".equals(panel.UZr())) return original;
        eeY role = palette.cfM("Device Main Body");
        return role == null ? original : role;
    }

    static iok graphiteFilledSurfaceM2(dve widget, int maxHops) {
        dve p = widget;
        for (int hops = 0; hops < maxHops && p != null; hops++) {
            p = p.RbC();
            if (p instanceof aOl) {
                aOl group = (aOl) p;
                iBH model = group.nfq();
                if (model == null || model.hUR() != zaq.MRY) continue;
                Hfk brightness = model.LaE();
                eff palette = group.PEn();
                if (brightness == null || palette == null) return null;
                eeY role = graphiteMainBodyM2((eeY) aQG.cfM(palette).apply(brightness), model, palette);
                iok fill = role == null ? null : role.cfM(group);
                return fill != null && fill.iHb() ? fill : null;
            }
        }
        return null;
    }

    static iok graphiteFlatChipM2(dve widget) {
        dve p = widget;
        for (int hops = 0; hops < 4; hops++) {
            p = p.RbC();
            if (p == null) return null;
            if (p instanceof dBX) {
                dBX chip = (dBX) p;
                egC style = chip.SI();
                if (style == null || !"Flat".equals(style.uva())) return null;
                eeM state = chip.AVR();
                if (state == null || state.ODA() == null) return null;
                eeY role = state.iHb();
                iok fill = role == null ? null : role.cfM(chip);
                return fill != null && fill.iHb() ? fill : null;
            }
        }
        return null;
    }

    static boolean graphiteChromaticM2(iok ink) {
        int r = ink.hbP(), g = ink.Grk(), b = ink.tp2();
        return Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) >= 48;
    }

    static float graphiteHueChannelM2(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 0.5f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    static iok graphiteHueInkM2(iok original, iok surface, iok fallback) {
        float r = original.hbP() / 255f, g = original.Grk() / 255f, b = original.tp2() / 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        float l = (max + min) / 2f, d = max - min;
        if (d <= 0f) return fallback;
        float s = l <= 0.5f ? d / (max + min) : d / (2f - max - min);
        float h;
        if (max == r) h = ((g - b) / d + (g < b ? 6f : 0f)) / 6f;
        else if (max == g) h = ((b - r) / d + 2f) / 6f;
        else h = ((r - g) / d + 4f) / 6f;
        for (int step = 1; step <= 200; step++) {
            float ll = l - 0.005f * step;
            if (ll <= 0f) break;
            float q = ll < 0.5f ? ll * (1f + s) : ll + s - ll * s;
            float p = 2f * ll - q;
            int nr = Math.round(graphiteHueChannelM2(p, q, h + 1f / 3f) * 255f);
            int ng = Math.round(graphiteHueChannelM2(p, q, h) * 255f);
            int nb = Math.round(graphiteHueChannelM2(p, q, h - 1f / 3f) * 255f);
            iok ink = new iok(0xff000000 | (Math.max(0, Math.min(255, nr)) << 16)
                | (Math.max(0, Math.min(255, ng)) << 8) | Math.max(0, Math.min(255, nb)));
            if (graphiteWcagM2(ink, surface) >= 5.0) return ink;
        }
        return fallback;
    }

    /* BatchMPaint: dut labels inside a "Display Button Group Chooser" (Display Background surface). */
    static iok graphiteDisplayGroupM(iok original, dve widget) {
        if (original != null && widget != null) {
            dve p = widget;
            for (int hops = 0; hops < 4; hops++) {
                p = p.RbC();
                if (p == null) break;
                if (p instanceof dBv) {
                    eeG style = ((dBv) p).EUG();
                    if (style != null && "Display Button Group Chooser".equals(style.uva())) {
                        eeY role = widget.PEn().Ai();
                        iok fill = role == null ? null : role.cfM(widget);
                        if (fill != null && fill.iHb()) return graphiteContrastOnM(original, fill);
                    }
                    break;
                }
            }
        }
        return graphiteGeneralContrast(original, widget);
    }

    /* BatchM2Paint: dut.cfM(SYo) label ink. */
    static iok graphiteLabelM2(iok original, dve widget) {
        if (original != null && widget != null) {
            iok chip = graphiteFlatChipM2(widget);
            if (chip != null) return graphiteContrastOnM(original, chip);
            Class<?> type = widget.getClass();
            if (type == duY.class || type == dvb.class) {
                efu font = ((duu) widget).VpW();
                if (font != null && "Bold Smaller".equals(font.uva()) && graphiteChromaticM2(original)) {
                    iok surface = graphiteFilledSurfaceM2(widget, 8);
                    if (surface != null && surface.MRY() >= 0.45f) {
                        iok tint = graphiteContrastOnM(original, surface);
                        if (tint != original) return graphiteHueInkM2(original, surface, tint);
                    }
                }
            }
        }
        return graphiteDisplayGroupM(original, widget);
    }

    /* BatchM2Paint: dND.cfM(String,SYo,FloatRectangle) ink. */
    static iok graphiteMultiFieldM2(iok original, dve widget) {
        if (original == null || !(widget instanceof dND)) return original;
        dND field = (dND) widget;
        iok surface = null;
        egp style = field.AU3();
        if (style != null) {
            eeM state = style.cfM(field.iHb);
            eeY role = state == null ? null : state.iHb();
            iok fill = role == null ? null : role.cfM(field);
            if (fill != null && fill.iHb()) surface = fill;
        }
        if (surface == null) surface = graphiteFilledSurfaceM2(field, 8);
        return surface == null ? original : graphiteContrastOnM(original, surface);
    }

    /* BatchMPaint: dNN.Ktv()/EUG() ink on a "Display" field's own fill. */
    static iok graphiteDisplayFieldM(iok original, dve widget) {
        iok surface = null;
        if (widget instanceof dNN) {
            egp style = ((dNN) widget).AU3();
            if (style != null && "Display".equals(style.uva())) {
                eeM normal = style.iHb();
                if (normal != null && normal.ODA() != null) {
                    eeY role = normal.iHb();
                    iok fill = role == null ? null : role.cfM(widget);
                    if (fill != null && fill.iHb()) surface = fill;
                }
            }
        }
        return surface == null ? graphiteGeneralContrast(original, widget) : graphiteContrastOnM(original, surface);
    }

    /* ---------- ReadabilityPaintH: disabled look of Inspector panel-colour fields ---------- */

    static boolean graphiteInspectorH(dNN widget) {
        iok background = widget.YxM();
        if (background == null || !background.iHb() || background.qoU() != 0xffa5a5a5
            || widget.AU3() != widget.jk_().DxJ().iHb()) return false;
        eeY role = widget.FCz();
        eff palette = widget.PEn();
        return role != null && (role == palette.cfM("Panel Red") || role == palette.cfM("Panel Yellow")
            || role == palette.cfM("Panel Mint") || role == palette.cfM("Panel Purple"));
    }

    static void graphiteInspectorDisabledH(dNN widget, SYo graphics) {
        cNP filter = new cNQ(new cNR(.25), new dCQ(widget.YxM(), .12));
        widget.Hnt(new WzZ(graphics, filter));
    }

    /* ---------- ReadabilityPaintI: outer aoH wrapper of the same fields ---------- */

    static boolean graphiteOpaqueI(iok background, int argb) {
        return background != null && background.iHb() && background.qoU() == argb;
    }

    static boolean graphiteLocalPairI(aoH outer, dNN inner) {
        return outer != null && outer.getClass() == aoH.class && inner != null
            && outer.E1b() == inner && inner.RbC() == outer && !outer.jm_()
            && outer.Mlm() == 1.0 && inner.Mlm() == 1.0;
    }

    static boolean graphiteDelayI(aoH outer, dNN inner) {
        return graphiteLocalPairI(outer, inner)
            && inner.AU3() == outer.jk_().DxJ().wGc()
            && inner.FCz() == outer.PEn().cfM("Panel Red")
            && "0 ms".equals(inner.zxZ())
            && graphiteOpaqueI(outer.YxM(), 0xff242424)
            && graphiteOpaqueI(inner.YxM(), 0xff242424);
    }

    static boolean graphiteOuterInspectorI(aoH outer, dNN inner) {
        if (!graphiteLocalPairI(outer, inner)
            || !graphiteOpaqueI(outer.YxM(), 0xffa5a5a5)
            || !graphiteOpaqueI(inner.YxM(), 0xffa5a5a5)
            || inner.AU3() != inner.jk_().DxJ().iHb()) return false;
        eeY role = inner.FCz();
        eff palette = inner.PEn();
        return role != null && (role == palette.cfM("Panel Red") || role == palette.cfM("Panel Yellow")
            || role == palette.cfM("Panel Mint") || role == palette.cfM("Panel Purple"));
    }

    static boolean graphiteOuterI(aoH outer) {
        dNN inner = outer.E1b();
        return graphiteOuterInspectorI(outer, inner) || graphiteDelayI(outer, inner);
    }

    static void graphiteOuterDisabledI(aoH outer, SYo graphics) {
        cNP filter = new cNQ(new cNR(.25), new dCQ(outer.YxM(), .12));
        outer.Hnt(new WzZ(graphics, filter));
    }

    /* ---------- EqInspectorRevisionE: EQ+ Inspector chooser text on #d6d6d6 ---------- */

    private static final String EQ_PLUS_UUID = "e4815188-ba6f-4d14-bcfc-2dcb8f778ccb";

    static boolean eqPlusInspector(aOy widget) {
        try {
            icl owner = widget.iAj();
            if (owner == null) return false;
            dfh panel = owner.iCS();
            if (!(panel instanceof Fot) || !"Inspector".equals(((Fot) panel).UZr())) return false;
            Object def = panel.IwV(gvB.class);
            if (!(def instanceof gvB)) return false;
            ES device = ((gvB) def).lYc();
            return device != null && EQ_PLUS_UUID.equals(String.valueOf(device.UNj()));
        } catch (RuntimeException | AssertionError e) {
            return false;
        }
    }
}
