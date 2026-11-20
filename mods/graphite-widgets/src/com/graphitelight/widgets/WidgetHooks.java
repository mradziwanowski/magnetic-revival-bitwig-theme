package com.graphitelight.widgets;

import bitwig.obf.aEG; // toggle/label paint widget (6.1.8: aEW)
import bitwig.obf.aOy; // enum/list chooser widget (6.1.8: aOL)
import bitwig.obf.aoH; // outer chooser panel (6.1.8: aoV)
import bitwig.obf.dND; // multi-value number field (6.1.8: dPc)
import bitwig.obf.dNN; // enum/value field (6.1.8: dPm)
import bitwig.obf.dnF; // padding wrapper (6.1.8: doU)
import bitwig.obf.aBk; // action button builder
import bitwig.obf.aCf; // top toolbar / transport builder
import bitwig.obf.aLZ; // text input builder
import bitwig.obf.aOV; // EQ curve widget
import bitwig.obf.aOl; // device group panel widget
import bitwig.obf.aSD; // Peak Limiter trace widget
import bitwig.obf.aSz; // device curve widget
import bitwig.obf.akZ; // splitter layout builder
import bitwig.obf.akw; // layout pane interface
import bitwig.obf.alq; // single-monitor layout builder
import bitwig.obf.als; // small-monitor layout builder
import bitwig.obf.bAE; // browser/panel header builder
import bitwig.obf.bdO; // launcher item / scene row widget
import bitwig.obf.cEk; // browser label builder
import bitwig.obf.cNK; // knob vector/bitmap painter
import bitwig.obf.dLB; // popup owner interface
import bitwig.obf.dLC; // parameter field popup builder
import bitwig.obf.dNX; // numeric parameter field widget
import bitwig.obf.dNn; // modulator/display plot widget
import bitwig.obf.dNv; // rotary knob widget
import bitwig.obf.dNw; // cached rotary knob surface
import bitwig.obf.dRG; // search/tag input widget
import bitwig.obf.dRZ; // browser search bar widget
import bitwig.obf.dVx; // splitter anchor side enum (cfM = LEFT/FIRST, MRY = RIGHT/SECOND)
import bitwig.obf.dWb; // box layout container
import bitwig.obf.dqT; // SVG icon widget
import bitwig.obf.dqw; // panel gradient overlay widget
import bitwig.obf.dre; // panel title builder
import bitwig.obf.drh; // header/label style helper
import bitwig.obf.dut; // text label widget
import bitwig.obf.dve; // base widget
import bitwig.obf.dzv; // checkbox/toggle widget
import bitwig.obf.ecz; // search field widget
import bitwig.obf.eeB; // border/frame style registry
import bitwig.obf.eeM; // style box/bevel descriptor
import bitwig.obf.eeY; // named colour role
import bitwig.obf.eex; // frame/border descriptor
import bitwig.obf.efU; // chrome gradient style builder
import bitwig.obf.efX; // display style builder
import bitwig.obf.eff; // theme colour palette
import bitwig.obf.egS; // button/box style state builder
import bitwig.obf.ehb; // display text/button style builder
import bitwig.obf.eiH; // style sheet registry
import bitwig.obf.eif; // text field style
import bitwig.obf.eii; // text field style registry
import bitwig.obf.eim; // box style builder
import bitwig.obf.ejt; // paintable role interface
import bitwig.obf.iBH; // device group layout model
import bitwig.obf.iok; // ARGB colour
import bml.api.Arg;
import bml.api.At;
import bml.api.Bml;
import bml.api.Hook;
import bml.api.Return;
import bml.api.This;
import com.bitwig.flt.widget.core.device.MVg; // device slot widget
import com.bitwig.flt.widget.core.device.dBl; // device header widget
import com.bitwig.flt.widget.core.device.g2L; // mixer device row / header
import com.bitwig.flt.widget.core.device.z2y; // device chain grid/frame
import com.bitwig.flt.widget.theme.puu; // tree/list theme builder
import com.bitwig.graphics.SYo; // graphics context

/** All BML hooks for graphite-widgets. */
public final class WidgetHooks {
    private static final Bml.Mod MOD = Bml.mod("graphite-widgets");

    private static final ThreadLocal<Boolean> EQ_FLAG = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<aOV> CUR_EQ_CURVE = new ThreadLocal<>();
    private static final ThreadLocal<aOl> CUR_PANEL = new ThreadLocal<>();
    private static final ThreadLocal<dqT> CUR_ICON = new ThreadLocal<>();
    private static final ThreadLocal<dNv> CUR_KNOB = new ThreadLocal<>();
    private static final ThreadLocal<dLB> CUR_POPUP_OWNER = new ThreadLocal<>();
    private static final ThreadLocal<aSD> CUR_TRACE = new ThreadLocal<>();
    private static final ThreadLocal<aSz> CUR_CURVE = new ThreadLocal<>();
    private static final ThreadLocal<dNn> CUR_PLOT = new ThreadLocal<>();

    private WidgetHooks() {}

    /* ---------- 1. Transport panel on Grey 5 & compact toolbar gap ---------- */

    @Hook(id = "acf-transport-grey5", target = aCf.class,
          method = "cfM(LaCp;Lcom/bitwig/flt/document/core/iface/pHd;Lcom/bitwig/ramona/core/lBQ;)Ldve;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Ai()LeeY;", occurrence = 1)
    public static eeY transportBg(eff palette) {
        return palette.iHb(); // Grey 5 (#a5a5a5) instead of Display Background (#242424)
    }

    @Hook(id = "acf-toolbar-gap", target = aCf.class, method = "MRY(LaBW;LaCp;Z)V",
          at = At.REPLACE_CALL, callOwner = dWb.class, call = "IwV(I)LdWb;", occurrence = 1)
    public static dWb toolbarGap(int gap) {
        return dWb.IwV(gap == 8 ? 4 : gap);
    }

    // v07n17 aCf.MRY: second row gap 8 -> 4, group paddings (8,8,2,2) -> (4,4,2,2),
    // and the whole toolbar row's last padding 5 -> 1 (the row is 4 pt shorter).
    @Hook(id = "acf-toolbar-gap-2", target = aCf.class, method = "MRY(LaBW;LaCp;Z)V",
          at = At.REPLACE_CALL, callOwner = dWb.class, call = "IwV(I)LdWb;", occurrence = 2)
    public static dWb toolbarGap2(int gap) {
        return dWb.IwV(gap == 8 ? 4 : gap);
    }

    @Hook(id = "acf-toolbar-pad-1", target = aCf.class, method = "MRY(LaBW;LaCp;Z)V",
          at = At.REPLACE_CALL, callOwner = dnF.class, call = "cfM(Ldve;IIII)LdnF;", occurrence = 1)
    public static dnF toolbarPad1(dve w, int a, int b, int c, int d) {
        return dnF.cfM(w, a == 8 ? 4 : a, b == 8 ? 4 : b, c, d);
    }

    @Hook(id = "acf-toolbar-pad-2", target = aCf.class, method = "MRY(LaBW;LaCp;Z)V",
          at = At.REPLACE_CALL, callOwner = dnF.class, call = "cfM(Ldve;IIII)LdnF;", occurrence = 2)
    public static dnF toolbarPad2(dve w, int a, int b, int c, int d) {
        return dnF.cfM(w, a == 8 ? 4 : a, b == 8 ? 4 : b, c, d);
    }

    @Hook(id = "acf-toolbar-pad-3", target = aCf.class, method = "MRY(LaBW;LaCp;Z)V",
          at = At.REPLACE_CALL, callOwner = dnF.class, call = "cfM(Ldve;IIII)LdnF;", occurrence = 3)
    public static dnF toolbarPad3(dve w, int a, int b, int c, int d) {
        return dnF.cfM(w, a, b, c, d == 5 ? 1 : d);
    }

    /* ---------- 2. Left-side browser layout (left-side position) ---------- */

    @Hook(id = "alq-browser-splitter-swap", target = alq.class, method = "cfM()LajC;",
          at = At.REPLACE_CALL, callOwner = akZ.class,
          call = "cfM(Ljava/lang/String;ZLakw;Lakw;)LakZ;", occurrence = 1)
    public static akZ alqSwapBrowser(String id, boolean horizontal, akw first, akw second) {
        if ("main_and_browser_splitter".equals(id)) {
            return akZ.cfM(id, horizontal, second, first); // browser on left
        }
        return akZ.cfM(id, horizontal, first, second);
    }

    @Hook(id = "alq-browser-anchor-left", target = alq.class, method = "cfM()LajC;",
          at = At.REPLACE_CALL, callOwner = akZ.class, call = "cfM(LdVx;)V", occurrence = 1)
    public static void alqBrowserAnchor(akZ splitter, dVx side) {
        splitter.cfM(side == dVx.MRY ? dVx.cfM : side);
    }

    @Hook(id = "als-browser-splitter-swap", target = als.class, method = "cfM()LakC;",
          at = At.REPLACE_CALL, callOwner = akZ.class,
          call = "cfM(Ljava/lang/String;ZLakw;Lakw;)LakZ;", occurrence = 1)
    public static akZ alsSwapBrowser(String id, boolean horizontal, akw first, akw second) {
        if ("main_and_browser_splitter".equals(id)) {
            return akZ.cfM(id, horizontal, second, first);
        }
        return akZ.cfM(id, horizontal, first, second);
    }

    @Hook(id = "als-browser-anchor-left", target = als.class, method = "cfM()LakC;",
          at = At.REPLACE_CALL, callOwner = akZ.class, call = "cfM(LdVx;)V", occurrence = 1)
    public static void alsBrowserAnchor(akZ splitter, dVx side) {
        splitter.cfM(side == dVx.MRY ? dVx.cfM : side);
    }

    /* ---------- 3. Neutral light scene rows (Graphite default) ---------- */

    @Hook(id = "bdo-neutral-scene-item", target = bdO.class, method = "a_(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = bdO.class, call = "pYg()Liok;", occurrence = 1)
    public static iok neutralSceneColor(bdO item) {
        return WidgetLogic.neutralSceneItem(item.pYg());
    }

    /* ---------- 4. Flat device headers & Graphite Device Main Body ---------- */

    @Hook(id = "dbl-flat-header-role", target = dBl.class, method = "Ktv()V",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "cfM(Ljava/lang/String;)LeeY;", occurrence = 1)
    public static eeY deviceHeaderRole(eff palette, String ignoredName) {
        return palette.hQi(); // unified flat header role
    }

    @Hook(id = "g2l-flat-header-top", target = g2L.class, method = "a_(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = iok.class, call = "hUR(FFF)Liok;", occurrence = 1)
    public static iok flatHeaderGradTop(iok base, float dh, float ds, float db) {
        return base;
    }

    @Hook(id = "g2l-flat-header-bot", target = g2L.class, method = "a_(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = iok.class, call = "hUR(FFF)Liok;", occurrence = 2)
    public static iok flatHeaderGradBot(iok base, float dh, float ds, float db) {
        return base;
    }

    @Hook(id = "z2y-frame-stroke", target = z2y.class, method = "IwV(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Mb2()LeeY;", occurrence = 1)
    public static eeY z2yFrameStroke(eff palette) {
        return palette.Scv();
    }

    @Hook(id = "mvg-slot-bg", target = MVg.class, method = "ERO()V",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "bax()LeeY;", occurrence = 1)
    public static eeY mvgSlotBg(eff palette) {
        return palette.lYc();
    }

    @Hook(id = "aol-paint-entry", target = aOl.class, method = "a_(Lcom/bitwig/graphics/SYo;)V", at = At.ENTRY)
    public static void onPanelPaintEntry(@This aOl panel) {
        CUR_PANEL.set(panel);
    }

    @Hook(id = "aol-paint-body-role", target = aOl.class, method = "a_(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok panelBodyColor(eeY role, dve widget) {
        try {
            aOl panel = CUR_PANEL.get();
            if (panel != null) {
                role = WidgetLogic.mainBodyRole(role, panel.nfq(), panel.PEn());
            }
        } catch (Throwable ignored) {}
        return role.cfM(widget);
    }

    @Hook(id = "aol-skip-grad-top", target = aOl.class, method = "a_(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = iBH.class, call = "E3()Z", occurrence = 1)
    public static boolean panelSkipGradE3(iBH model) { return false; }

    @Hook(id = "aol-skip-grad-bot", target = aOl.class, method = "a_(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = iBH.class, call = "lYc()Z", occurrence = 1)
    public static boolean panelSkipGradLyc(iBH model) { return false; }

    /* ---------- 4b. Suppress panel gradient overlay widget (dqw: Phase-4 & Compressor+) ---------- */

    @Hook(id = "dqw-suppress-gradient-fill", target = dqw.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = SYo.class, call = "wGc()V", occurrence = 1)
    public static void dqwSuppressFill(SYo graphics) {
        graphics.nVd(); // clear pending gradient path without filling
    }

    /* ---------- 4c. Graphite flat ring knobs (dNv & dNw: ControlsRevisionD) ---------- */

    @Hook(id = "dnv-suppress-fallback-body", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = cNK.class,
          call = "cfM(DDFLiok;Liok;FLiok;Liok;)V", occurrence = 1)
    public static void dnvSuppressFallbackBody(cNK p, double x, double y, float r, iok c1, iok c2, float w, iok c3, iok c4) {
        // intentionally suppressed: flat Graphite ring knob
    }

    @Hook(id = "dnv-arc-start", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = Math.class, call = "toRadians(D)D", occurrence = 1)
    public static double dnvArcStart(double deg) {
        return Math.toRadians(deg == 130.0 ? 135.0 : deg);
    }

    @Hook(id = "dnv-arc-end", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = Math.class, call = "toRadians(D)D", occurrence = 2)
    public static double dnvArcEnd(double deg) {
        return Math.toRadians(deg == 410.0 ? 405.0 : deg);
    }

    // v07n17: JqA always returns the style's value-arc role (cyan); stock returns the knob's own
    // hue (FCz) from an early return, so this must wrap every return.
    @Hook(id = "dnv-jqa-cyan", target = dNv.class, method = "JqA()LeeY;", at = At.WRAP_RETURN, allReturns = true)
    public static eeY dnvJqaCyan(@Return eeY stock, @This dNv knob) {
        try {
            eeY cyan = knob.gfw().ODA();
            if (cyan != null) return cyan;
        } catch (Throwable ignored) {}
        return stock;
    }

    // v07n17 graphiteNeutralH: on a dark surface, track ring, dial fill, needle and default label
    // go through the theme's contrast picker with #c8c8c8 as the alternative.
    private static final iok GRAPHITE_KNOB_NEUTRAL = new iok(0xffc8c8c8);

    static iok knobNeutral(iok colour, dve widget) {
        try {
            iok bg = widget.YxM();
            if (bg == null || !bg.iHb()) return colour;
            return widget.PEn().cfM(bg, colour, GRAPHITE_KNOB_NEUTRAL);
        } catch (Throwable ignored) {
            return colour;
        }
    }

    @Hook(id = "dnv-dial-fill-neutral", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok dnvDialFillNeutral(eeY role, dve widget) {
        return knobNeutral(role.cfM(widget), widget);
    }

    @Hook(id = "dnv-track-ring-neutral", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 4)
    public static iok dnvTrackRingNeutral(eeY role, dve widget) {
        return knobNeutral(role.cfM(widget), widget);
    }

    @Hook(id = "dnv-label-neutral", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 8)
    public static iok dnvLabelNeutral(eeY role, dve widget) {
        iok colour = role.cfM(widget);
        try {
            if (widget instanceof dNv knob && knob.HL == null) return knobNeutral(colour, widget);
        } catch (Throwable ignored) {}
        return colour;
    }

    // v07n17 needle: no shadow (alpha 0) and 1.5 x scale instead of 2.5 x scale.
    @Hook(id = "dnv-needle-no-shadow", target = dNv.class, method = "MRY(Lcom/bitwig/graphics/SYo;D)V",
          at = At.REPLACE_CALL, callOwner = iok.class, call = "cfM(F)Liok;", occurrence = 1)
    public static iok dnvNeedleNoShadow(iok colour, float alpha) {
        return colour.cfM(0f);
    }

    @Hook(id = "dnv-needle-shadow-width", target = dNv.class, method = "MRY(Lcom/bitwig/graphics/SYo;D)V",
          at = At.REPLACE_CALL, callOwner = SYo.class, call = "IwV(D)V", occurrence = 1)
    public static void dnvNeedleShadowWidth(SYo g, double width) {
        g.IwV((width - 2.0) * 0.6 + 2.0);
    }

    @Hook(id = "dnv-needle-width", target = dNv.class, method = "MRY(Lcom/bitwig/graphics/SYo;D)V",
          at = At.REPLACE_CALL, callOwner = SYo.class, call = "IwV(D)V", occurrence = 2)
    public static void dnvNeedleWidth(SYo g, double width) {
        g.IwV(width * 0.6);
    }

    @Hook(id = "dnv-needle-entry", target = dNv.class, method = "MRY(Lcom/bitwig/graphics/SYo;D)V", at = At.ENTRY)
    public static void dnvNeedleEntry(@This dNv knob) {
        CUR_KNOB.set(knob);
    }

    @Hook(id = "dnv-needle-color-1", target = dNv.class, method = "MRY(Lcom/bitwig/graphics/SYo;D)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok dnvNeedleColor1(eeY role, dve widget) {
        try {
            dNv knob = CUR_KNOB.get();
            if (knob != null) return knobNeutral(knob.gfw().MIO().cfM(knob), knob);
        } catch (Throwable ignored) {}
        return role.cfM(widget);
    }

    @Hook(id = "dnv-needle-color-2", target = dNv.class, method = "MRY(Lcom/bitwig/graphics/SYo;D)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 2)
    public static iok dnvNeedleColor2(eeY role, dve widget) {
        try {
            dNv knob = CUR_KNOB.get();
            if (knob != null) return knobNeutral(knob.gfw().MIO().cfM(knob), knob);
        } catch (Throwable ignored) {}
        return role.cfM(widget);
    }

    @Hook(id = "dnv-angle-270", target = dNv.class, method = "IwV(D)D",
          at = At.REPLACE_CALL, callOwner = Math.class, call = "toRadians(D)D", occurrence = 1)
    public static double dnvAngle270(double deg) {
        return Math.toRadians(135.0 + (deg - 130.0) * (270.0 / 280.0));
    }

    @Hook(id = "dnv-mod-arc-1", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;Liok;DZ)V",
          at = At.REPLACE_CALL, callOwner = Math.class, call = "toRadians(D)D", occurrence = 1)
    public static double dnvModArc1(double deg) {
        return Math.toRadians(135.0 + (deg - 130.0) * (270.0 / 280.0));
    }

    @Hook(id = "dnv-mod-arc-2", target = dNv.class, method = "cfM(Lcom/bitwig/graphics/SYo;Liok;DZ)V",
          at = At.REPLACE_CALL, callOwner = Math.class, call = "toRadians(D)D", occurrence = 2)
    public static double dnvModArc2(double deg) {
        return Math.toRadians(135.0 + (deg - 130.0) * (270.0 / 280.0));
    }

    @Hook(id = "dnw-arc-start", target = dNw.class,
          method = "<init>(Lcom/bitwig/windowing_system/Display;LefU;LefM;Liok;Liok;Liok;Liok;II)V",
          at = At.REPLACE_CALL, callOwner = Math.class, call = "toRadians(D)D", occurrence = 1)
    public static double dnwArcStart(double deg) {
        return Math.toRadians(deg == 130.0 ? 135.0 : deg);
    }

    @Hook(id = "dnw-arc-end", target = dNw.class,
          method = "<init>(Lcom/bitwig/windowing_system/Display;LefU;LefM;Liok;Liok;Liok;Liok;II)V",
          at = At.REPLACE_CALL, callOwner = Math.class, call = "toRadians(D)D", occurrence = 2)
    public static double dnwArcEnd(double deg) {
        return Math.toRadians(deg == 410.0 ? 405.0 : deg);
    }

    @Hook(id = "dnw-suppress-shadow", target = dNw.class,
          method = "<init>(Lcom/bitwig/windowing_system/Display;LefU;LefM;Liok;Liok;Liok;Liok;II)V",
          at = At.REPLACE_CALL, callOwner = cNK.class, call = "MRY(DDFFLiok;)V", occurrence = 1)
    public static void dnwSuppressShadow(cNK p, double x, double y, float r, float blur, iok color) {
        // intentionally suppressed: no 3D knob drop shadow
    }

    @Hook(id = "dnw-suppress-body-1", target = dNw.class,
          method = "<init>(Lcom/bitwig/windowing_system/Display;LefU;LefM;Liok;Liok;Liok;Liok;II)V",
          at = At.REPLACE_CALL, callOwner = cNK.class,
          call = "cfM(DDFLiok;Liok;FLiok;Liok;)V", occurrence = 1)
    public static void dnwSuppressBody1(cNK p, double x, double y, float r, iok c1, iok c2, float w, iok c3, iok c4) {
        // intentionally suppressed: flat Graphite ring knob
    }

    @Hook(id = "dnw-suppress-body-2", target = dNw.class,
          method = "<init>(Lcom/bitwig/windowing_system/Display;LefU;LefM;Liok;Liok;Liok;Liok;II)V",
          at = At.REPLACE_CALL, callOwner = cNK.class,
          call = "cfM(DDFLiok;Liok;FLiok;Liok;)V", occurrence = 2)
    public static void dnwSuppressBody2(cNK p, double x, double y, float r, iok c1, iok c2, float w, iok c3, iok c4) {
        // intentionally suppressed: flat Graphite ring knob
    }

    /* ---------- 4f. v07n17 label/field ink helpers (dut, dND, dNN, aoH, aEG, aOy) ---------- */

    // dut.cfM(SYo): both branches of the label ink (explicit role / palette ghS) -> graphiteLabelM2.
    @Hook(id = "dut-label-ink-1", target = dut.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok dutLabelInk1(eeY role, dve widget) {
        return V07Paint.graphiteLabelM2(role.cfM(widget), widget);
    }

    @Hook(id = "dut-label-ink-2", target = dut.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 2)
    public static iok dutLabelInk2(eeY role, dve widget) {
        return V07Paint.graphiteLabelM2(role.cfM(widget), widget);
    }

    @Hook(id = "dnd-field-ink-1", target = dND.class,
          method = "cfM(Ljava/lang/String;Lcom/bitwig/graphics/SYo;Lcom/bitwig/base/geom/FloatRectangle;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok dndFieldInk1(eeY role, dve widget) {
        return V07Paint.graphiteMultiFieldM2(role.cfM(widget), widget);
    }

    @Hook(id = "dnd-field-ink-2", target = dND.class,
          method = "cfM(Ljava/lang/String;Lcom/bitwig/graphics/SYo;Lcom/bitwig/base/geom/FloatRectangle;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 2)
    public static iok dndFieldInk2(eeY role, dve widget) {
        return V07Paint.graphiteMultiFieldM2(role.cfM(widget), widget);
    }

    @Hook(id = "dnn-ktv-display-field", target = dNN.class, method = "Ktv()Liok;", at = At.WRAP_RETURN, allReturns = true)
    public static iok dnnKtv(@Return iok colour, @This dNN field) {
        return V07Paint.graphiteDisplayFieldM(colour, field);
    }

    @Hook(id = "dnn-eug-display-field", target = dNN.class, method = "EUG()Liok;", at = At.WRAP_RETURN, allReturns = true)
    public static iok dnnEug(@Return iok colour, @This dNN field) {
        return V07Paint.graphiteDisplayFieldM(colour, field);
    }

    @Hook(id = "dnn-inspector-disabled", target = dNN.class, method = "c_(Lcom/bitwig/graphics/SYo;)V", at = At.OVERRIDE_INSTEAD)
    public static void dnnPaint(@This dNN field, @Arg(0) SYo graphics) {
        try {
            if (V07Paint.graphiteInspectorH(field)) {
                V07Paint.graphiteInspectorDisabledH(field, graphics);
                return;
            }
            V07Super.DNN_PAINT.invokeExact(field, graphics);
        } catch (Throwable t) {
            sneaky(t);
        }
    }

    @Hook(id = "aoh-outer-disabled", target = aoH.class, method = "c_(Lcom/bitwig/graphics/SYo;)V", at = At.OVERRIDE_INSTEAD)
    public static void aohPaint(@This aoH outer, @Arg(0) SYo graphics) {
        try {
            if (V07Paint.graphiteOuterI(outer)) {
                V07Paint.graphiteOuterDisabledI(outer, graphics);
                return;
            }
            V07Super.AOH_PAINT.invokeExact(outer, graphics);
        } catch (Throwable t) {
            sneaky(t);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneaky(Throwable t) throws T {
        throw (T) t;
    }

    // aEG.cfM(SYo): x3t text ink -> #c8c8c8 contrast pick on a dark own background (v07n17 aEW).
    @Hook(id = "aeg-text-neutral", target = aEG.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok aegTextNeutral(eeY role, dve widget) {
        return knobNeutral(role.cfM(widget), widget);
    }

    // aOy.cfM(DDDD): EQ+ Inspector chooser fill #d6d6d6 (v07n17 aOL).
    private static final iok EQ_INSPECTOR_FILL = new iok(214 / 255d, 214 / 255d, 214 / 255d);

    @Hook(id = "aoy-eq-inspector-fill", target = aOy.class, method = "cfM(DDDD)[D",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok aoyEqInspectorFill(eeY role, dve widget) {
        iok colour = role.cfM(widget);
        return widget instanceof aOy chooser && V07Paint.eqPlusInspector(chooser) ? EQ_INSPECTOR_FILL : colour;
    }

    /* ---------- 4d. Flat button bevels (eeB) & parameter field popup (dLC) ---------- */

    @Hook(id = "eeb-flat-button-bevel", target = eeB.class, method = "MRY(LeiH;)V",
          at = At.REPLACE_CALL, callOwner = eex.class,
          call = "cfM(Ljava/lang/String;LeeY;LeeY;LeeY;D)Leex;", occurrence = 1)
    public static eex eebButtonFlat(String name, eeY stroke, eeY highlight, eeY shadow, double radius) {
        if ("Button".equals(name)) {
            return eex.cfM(name, stroke, eeY.cfM, eeY.cfM, radius); // transparent bevel
        }
        return eex.cfM(name, stroke, highlight, shadow, radius);
    }

    // v07n17: "Rounded (Large)" (device bodies, radius 8) gets a 1 px stroke in the palette's
    // Scv role (6.1.8 vdO, #4f4f4f in Graphite Light); stock builds it without a stroke.
    private static final ThreadLocal<eiH> CUR_FRAME_SHEET = new ThreadLocal<>();

    @Hook(id = "eeb-frames-entry", target = eeB.class, method = "MRY(LeiH;)V", at = At.ENTRY)
    public static void eebFramesEntry(@Arg(0) eiH sheet) {
        CUR_FRAME_SHEET.set(sheet);
    }

    @Hook(id = "eeb-rounded-large-stroke", target = eeB.class, method = "MRY(LeiH;)V",
          at = At.REPLACE_CALL, callOwner = eex.class, call = "cfM(Ljava/lang/String;D)Leex;", occurrence = 1)
    public static eex eebRoundedLargeStroke(String name, double radius) {
        try {
            eiH sheet = CUR_FRAME_SHEET.get();
            if ("Rounded (Large)".equals(name) && sheet != null) {
                return eex.cfM(name, sheet.BAa().Scv(), radius, 1.0);
            }
        } catch (Throwable ignored) {}
        return eex.cfM(name, radius);
    }

    @Hook(id = "dlc-popup-entry", target = dLC.class, method = "MRY(LdLB;)V", at = At.ENTRY)
    public static void dlcPopupEntry(@Arg(0) dLB owner) {
        CUR_POPUP_OWNER.set(owner);
    }

    @Hook(id = "dlc-param-field-style", target = dLC.class, method = "MRY(LdLB;)V",
          at = At.REPLACE_CALL, callOwner = eii.class, call = "cfM(Ljava/lang/String;)Leif;", occurrence = 1)
    public static eif dlcParamFieldStyle(eii reg, String name) {
        eif style = reg.cfM(name);
        try {
            if (CUR_POPUP_OWNER.get() instanceof dNX && style != null) {
                eim copy = style.ODA();
                copy.MRY(eeY.cfM("Graphite parameter field local background", new iok(0xffd6d6d6)));
                if (copy instanceof eif f) {
                    f.cfM(eeY.cfM("Graphite parameter field local text", new iok(0xff161616)));
                    return f;
                }
            }
        } catch (Throwable ignored) {}
        return style;
    }

    /* ---------- 4e. Curve / trace contrast on dark displays (aSD, aSz, dNn) ---------- */

    @Hook(id = "asd-trace-entry", target = aSD.class, method = "zpW()Liok;", at = At.ENTRY)
    public static void asdEntry(@This aSD self) { CUR_TRACE.set(self); }

    @Hook(id = "asd-trace-contrast", target = aSD.class, method = "zpW()Liok;", at = At.WRAP_RETURN)
    public static iok asdTrace(@Return iok color) {
        try { return WidgetLogic.traceContrast(color, CUR_TRACE.get()); } catch (Throwable ignored) { return color; }
    }

    @Hook(id = "asz-curve-entry", target = aSz.class, method = "cfM(Lcom/bitwig/graphics/SYo;Z)V", at = At.ENTRY)
    public static void aszEntry(@This aSz self) { CUR_CURVE.set(self); }

    @Hook(id = "asz-curve-contrast", target = aSz.class, method = "cfM(Lcom/bitwig/graphics/SYo;Z)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 2)
    public static iok aszCurve(eeY role, dve widget) {
        iok c = role.cfM(widget);
        try { return WidgetLogic.curveContrast(c, CUR_CURVE.get()); } catch (Throwable ignored) { return c; }
    }

    @Hook(id = "dnn-plot-entry", target = dNn.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V", at = At.ENTRY)
    public static void dnnEntry(@This dNn self) { CUR_PLOT.set(self); }

    @Hook(id = "dnn-plot-contrast", target = dNn.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeY.class, call = "cfM(Ldve;)Liok;", occurrence = 1)
    public static iok dnnPlot(eeY role, dve widget) {
        iok c = role.cfM(widget);
        try { return WidgetLogic.traceContrast(c, CUR_PLOT.get()); } catch (Throwable ignored) { return c; }
    }

    @Hook(id = "aol-sut-entry", target = aOl.class, method = "sut()V", at = At.ENTRY)
    public static void onPanelSutEntry(@This aOl panel) {
        CUR_PANEL.set(panel);
    }

    @Hook(id = "aol-sut-body-role", target = aOl.class, method = "sut()V",
          at = At.REPLACE_CALL, callOwner = aOl.class, call = "UNj(Lejt;)V", occurrence = 2)
    public static void panelSutBg(aOl panel, ejt role) {
        try {
            if (role instanceof eeY named) {
                role = WidgetLogic.mainBodyRole(named, panel.nfq(), panel.PEn());
            }
        } catch (Throwable ignored) {}
        panel.UNj(role);
    }

    /* ---------- 5. Flat bevels / chrome & role rebindings ---------- */

    @Hook(id = "efu-flat-chrome", target = efU.class, method = "MRY(Ljava/lang/String;LeiH;)V",
          at = At.REPLACE_CALL, callOwner = efU.class,
          call = "cfM(LeeY;LeeY;LeeY;LeeY;LeeY;LeeY;LeeY;F)V", occurrence = 1)
    public static void efuFlat(efU self, eeY c1, eeY c2, eeY c3, eeY c4, eeY c5, eeY c6, eeY c7, float emboss) {
        self.cfM(c1, c2, c3, c4, c5, c6, c7, 0.0f);
    }

    @Hook(id = "egs-iwv-flat", target = egS.class, method = "IwV()LeeM;", at = At.WRAP_RETURN)
    public static eeM egsIwvFlat(@Return eeM box) {
        if (box != null) box.cfM(0.0);
        return box;
    }

    @Hook(id = "egs-nnn-flat", target = egS.class, method = "Nnn()LeeM;", at = At.WRAP_RETURN)
    public static eeM egsNnnFlat(@Return eeM box) {
        if (box != null) box.cfM(0.0);
        return box;
    }

    @Hook(id = "puu-cfm-role", target = puu.class,
          method = "cfM(Lcom/bitwig/flt/widget/theme/Rzi;)Lcom/bitwig/flt/widget/theme/puu;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "LaE()LeeY;", occurrence = 1)
    public static eeY puuCfM(eff p) { return p.PD3(); }

    @Hook(id = "puu-mry-role", target = puu.class,
          method = "MRY(Lcom/bitwig/flt/widget/theme/Rzi;)Lcom/bitwig/flt/widget/theme/puu;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "tp2()LeeY;", occurrence = 1)
    public static eeY puuMRY(eff p) { return p.cfM("Dark tree background (selected)"); }

    @Hook(id = "puu-hnt-role", target = puu.class,
          method = "Hnt(Lcom/bitwig/flt/widget/theme/Rzi;)Lcom/bitwig/flt/widget/theme/puu;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Ucl()LeeY;", occurrence = 1)
    public static eeY puuHnt(eff p) { return p.PD3(); }

    @Hook(id = "abk-text-role", target = aBk.class,
          method = "cfM(Ldve;Ljava/lang/String;LLJt;LAQw;Ljava/lang/Runnable;)Ldve;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY abkRole(eff p) { return p.Ktv(); }

    @Hook(id = "alz-bright-input", target = aLZ.class, method = "MRY(Lcom/bitwig/ramona/core/ESe;)LecV;",
          at = At.REPLACE_CALL, callOwner = eii.class, call = "IwV()Leif;", occurrence = 1)
    public static eif alzBright(eii reg) { return reg.cfM("Bright"); }

    @Hook(id = "bae-dark-text", target = bAE.class, method = "IwV(LTgk;)Ldve;",
          at = At.REPLACE_CALL, callOwner = bAE.class, call = "SQN(Ljava/lang/String;)LeeY;", occurrence = 1)
    public static eeY baeDarkText(bAE self, String name) {
        return self.SQN("Subtle Light Text".equals(name) ? "Dark Text" : name);
    }

    @Hook(id = "cek-label-1", target = cEk.class, method = "cfM(Lcom/bitwig/flt/library/metadata/master/iyP;)Ldve;",
          at = At.REPLACE_CALL, callOwner = dut.class, call = "TtY(Ljava/lang/String;)Ldut;", occurrence = 1)
    public static dut cek1(String s) { return dut.eLY(s); }

    @Hook(id = "cek-label-2", target = cEk.class, method = "cfM(Ljava/lang/String;Ljava/lang/Object;Z)Ldve;",
          at = At.REPLACE_CALL, callOwner = dut.class, call = "TtY(Ljava/lang/String;)Ldut;", occurrence = 1)
    public static dut cek2(String s) { return dut.eLY(s); }

    @Hook(id = "cek-label-3", target = cEk.class, method = "cfM(Ljava/lang/String;)Ldve;",
          at = At.REPLACE_CALL, callOwner = dut.class, call = "TtY(Ljava/lang/String;)Ldut;", occurrence = 1)
    public static dut cek3(String s) { return dut.eLY(s); }

    @Hook(id = "cek-label-4", target = cEk.class, method = "cfM(LJp;LFI;)Ldve;",
          at = At.REPLACE_CALL, callOwner = dut.class, call = "TtY(Ljava/lang/String;)Ldut;", occurrence = 1)
    public static dut cek4(String s) { return dut.eLY(s); }

    @Hook(id = "dre-label", target = dre.class, method = "IwV()Ldve;",
          at = At.REPLACE_CALL, callOwner = dut.class, call = "TtY(Ljava/lang/String;)Ldut;", occurrence = 1)
    public static dut dreLabel(String s) { return dut.eLY(s); }

    @Hook(id = "drh-role", target = drh.class, method = "gfw()V",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY drhRole(eff p) { return p.Ktv(); }

    @Hook(id = "drg-bright", target = dRG.class, method = "pYg()Leif;",
          at = At.REPLACE_CALL, callOwner = eii.class, call = "IwV()Leif;", occurrence = 1)
    public static eif drgBright(eii reg) { return reg.cfM("Bright"); }

    @Hook(id = "drg-role", target = dRG.class, method = "cfM(Leif;)V",
          at = At.REPLACE_CALL, callOwner = eif.class, call = "ERO()LeeY;", occurrence = 1)
    public static eeY drgRole(eif style) { return style.dnz(); }

    @Hook(id = "drz-search-bright", target = dRZ.class, method = "<init>()V",
          at = At.REPLACE_CALL, callOwner = ecz.class, call = "LzO()Lecz;", occurrence = 1)
    public static ecz drzSearchField() {
        ecz field = ecz.LzO();
        try {
            field.Hnt(field.jk_().LKb().cfM("Bright"));
        } catch (Throwable ignored) {}
        return field;
    }

    @Hook(id = "drz-bg", target = dRZ.class, method = "gfw()V",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Ai()LeeY;", occurrence = 1)
    public static eeY drzBg(eff p) { return p.lYc(); }

    @Hook(id = "ecz-search-icon-ink", target = ecz.class, method = "oc_()Ldve;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "x3t()LeeY;", occurrence = 1)
    public static eeY eczSearchIcon(eff p) {
        try {
            return WidgetLogic.searchIconInk(p);
        } catch (Throwable t) {
            return p.x3t();
        }
    }

    @Hook(id = "efx-hitcount", target = efX.class, method = "Hnt(LeiH;)LehE;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY efxHitcount(eff p) { return p.cfM("Hitcount text color"); }

    @Hook(id = "ehb-dei-1", target = ehb.class, method = "dEI(LeiH;)LegY;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY ehbDei1(eff p) { return p.cfM("Hitcount text color"); }

    @Hook(id = "ehb-dei-2", target = ehb.class, method = "dEI(LeiH;)LegY;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 2)
    public static eeY ehbDei2(eff p) { return p.cfM("Hitcount text color"); }

    @Hook(id = "ehb-z1c-bg", target = ehb.class, method = "Z1C(LeiH;)LegY;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "hDc()LeeY;", occurrence = 1)
    public static eeY ehbZ1cBg(eff p) { return p.MYI(); }

    @Hook(id = "ehb-z1c-text", target = ehb.class, method = "Z1C(LeiH;)LegY;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY ehbZ1cText(eff p) { return p.cfM("Hitcount text color"); }

    @Hook(id = "ehb-dnz-text", target = ehb.class, method = "dnz(LeiH;)LegY;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY ehbDnzText(eff p) { return p.cfM("Hitcount text color"); }

    @Hook(id = "ehb-hl-1", target = ehb.class, method = "HL(LeiH;)LegY;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY ehbHl1(eff p) { return p.cfM("Hitcount text color"); }

    @Hook(id = "ehb-hl-2", target = ehb.class, method = "HL(LeiH;)LegY;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 2)
    public static eeY ehbHl2(eff p) { return p.cfM("Hitcount text color"); }

    @Hook(id = "eih-button-stroke", target = eiH.class, method = "tB1()LehL;",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Dah()LeeY;", occurrence = 1)
    public static eeY eihButtonStroke(eff p) { return p.cfM("Button stroke"); }

    @Hook(id = "eim-unj-bg", target = eim.class, method = "UNj(Ljava/lang/String;LeiH;)V",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "Ai()LeeY;", occurrence = 1)
    public static eeY eimUnjBg(eff p) { return p.Scv(); }

    @Hook(id = "eim-unj-ink", target = eim.class, method = "UNj(Ljava/lang/String;LeiH;)V",
          at = At.REPLACE_CALL, callOwner = eff.class, call = "x3t()LeeY;", occurrence = 1)
    public static eeY eimUnjInk(eff p) { return p.MYI(); }

    /* ---------- 6. EQ+ cyan curve & v07n17 mixer bypass curve ---------- */

    @Hook(id = "aov-eq-color-entry", target = aOV.class, method = "cfM(DZ)Liok;", at = At.ENTRY)
    public static void eqColorEntry(@Arg(1) boolean secondary) {
        EQ_FLAG.set(secondary);
    }

    @Hook(id = "aov-eq-color-return", target = aOV.class, method = "cfM(DZ)Liok;", at = At.WRAP_RETURN)
    public static iok eqColorReturn(@Return iok stock) {
        boolean secondary = Boolean.TRUE.equals(EQ_FLAG.get());
        return new iok(secondary ? 0xff4dcbff : 0xff00d5ff);
    }

    @Hook(id = "aov-mixer-curve-entry", target = aOV.class, method = "hUR(Lcom/bitwig/graphics/SYo;)V", at = At.ENTRY)
    public static void eqCurvePaintEntry(@This aOV curve) {
        CUR_EQ_CURVE.set(curve);
    }

    @Hook(id = "aov-mixer-curve-stroke", target = aOV.class, method = "hUR(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = SYo.class, call = "hbP()V", occurrence = 1)
    public static void eqCurveStroke(SYo graphics) {
        try {
            WidgetLogic.mixerCurve(CUR_EQ_CURVE.get(), graphics);
        } catch (Throwable t) {
            MOD.log("mixerCurve failed", t);
        }
        graphics.hbP(); // original stroke
    }

    /* ---------- 7. v07n17 Device lamps (mixer + header) & header power repaint ---------- */

    @Hook(id = "dqt-paint-entry", target = dqT.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V", at = At.ENTRY)
    public static void iconPaintEntry(@This dqT icon) {
        CUR_ICON.set(icon);
    }

    @Hook(id = "dqt-paint-tint-mry", target = dqT.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = SYo.class, call = "MRY(Liok;)V", occurrence = 1)
    public static void iconTintMry(SYo graphics, iok color) {
        iok out = color;
        try {
            out = WidgetLogic.iconInk(color, CUR_ICON.get(), graphics);
        } catch (Throwable t) {
            MOD.log("chainIndicator(MRY) failed", t);
        }
        graphics.MRY(out);
    }

    @Hook(id = "dqt-paint-tint-hnt", target = dqT.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = SYo.class, call = "Hnt(Liok;)V", occurrence = 1)
    public static void iconTintHnt(SYo graphics, iok color) {
        iok out = color;
        try {
            out = WidgetLogic.iconInk(color, CUR_ICON.get(), graphics);
        } catch (Throwable t) {
            MOD.log("chainIndicator(Hnt) failed", t);
        }
        graphics.Hnt(out);
    }

    @Hook(id = "dqt-recheck-1", target = dqT.class, method = "cfM(Liok;)V", at = At.BEFORE_RETURN)
    public static void recheck1(@This dqT icon) {
        try { WidgetLogic.deferHeaderPowerRepaint(icon); } catch (Throwable ignored) {}
    }

    @Hook(id = "dqt-recheck-2", target = dqT.class, method = "MRY(Liok;)V", at = At.BEFORE_RETURN)
    public static void recheck2(@This dqT icon) {
        try { WidgetLogic.deferHeaderPowerRepaint(icon); } catch (Throwable ignored) {}
    }

    @Hook(id = "dqt-recheck-3", target = dqT.class, method = "cfM(LeeY;)V", at = At.BEFORE_RETURN)
    public static void recheck3(@This dqT icon) {
        try { WidgetLogic.deferHeaderPowerRepaint(icon); } catch (Throwable ignored) {}
    }

    @Hook(id = "dqt-recheck-4", target = dqT.class, method = "MRY(LeeY;)V", at = At.BEFORE_RETURN)
    public static void recheck4(@This dqT icon) {
        try { WidgetLogic.deferHeaderPowerRepaint(icon); } catch (Throwable ignored) {}
    }

    /* ---------- 8. v07n17 Inspector enable indicator ---------- */

    @Hook(id = "dzv-inspector-indicator", target = dzv.class, method = "cfM(Lcom/bitwig/graphics/SYo;)V",
          at = At.REPLACE_CALL, callOwner = eeM.class,
          call = "cfM(Lcom/bitwig/graphics/SYo;Ldve;DDDD)V", occurrence = 1)
    public static void inspectorCheckbox(eeM box, SYo graphics, dve widget, double x, double y, double w, double h) {
        box.cfM(graphics, widget, x, y, w, h); // original draw
        try {
            if (widget instanceof dzv btn) {
                WidgetLogic.inspectorIndicator(btn, graphics, w);
            }
        } catch (Throwable t) {
            MOD.log("inspectorIndicator failed", t);
        }
    }
}
