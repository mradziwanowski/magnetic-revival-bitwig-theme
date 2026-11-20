package com.graphitelight.inkguard;

import bitwig.obf.iok; // immutable ARGB colour
import bml.api.Arg;
import bml.api.At;
import bml.api.Hook;
import bml.api.This;
import com.bitwig.graphics.CHR; // image draw options
import com.bitwig.graphics.LWw; // fill rule enum
import com.bitwig.graphics.Ln2; // paint pattern interface
import com.bitwig.graphics.pHd; // icon mask image
import com.bitwig.graphics.qc1; // bitmap image
import com.bitwig.graphics.render_commands.Dgc; // custom text renderer delegate
import com.bitwig.graphics.render_commands.JXO; // render command recorder
import com.bitwig.graphics.render_commands.Uev; // gradient pattern builder

import java.util.EnumSet;

/** All BML hooks for graphite-ink-guard. */
public final class InkGuardHooks {
    private InkGuardHooks() {}

    @Hook(id = "jxo-translate", target = JXO.class, method = "FzB(DD)V", at = At.ENTRY)
    public static void onTranslate(@This JXO self, @Arg(0) double dx, @Arg(1) double dy) {
        InkGuardLogic.translate(self, dx, dy);
    }

    @Hook(id = "jxo-scale", target = JXO.class, method = "Hnt(DD)V", at = At.ENTRY)
    public static void onScale(@This JXO self, @Arg(0) double sx, @Arg(1) double sy) {
        InkGuardLogic.scale(self, sx, sy);
    }

    @Hook(id = "jxo-rotate", target = JXO.class, method = "MRY(D)V", at = At.ENTRY)
    public static void onRotate(@This JXO self, @Arg(0) double angle) {
        InkGuardLogic.rotate(self, angle);
    }

    @Hook(id = "jxo-color-fzb", target = JXO.class, method = "FzB(DDDD)V", at = At.ENTRY)
    public static void onColorFzB(@This JXO self, @Arg(0) double r, @Arg(1) double g, @Arg(2) double b, @Arg(3) double a) {
        InkGuardLogic.color(self, r, g, b, a);
    }

    @Hook(id = "jxo-color-cfm", target = JXO.class, method = "cfM(DDDD)V", at = At.ENTRY)
    public static void onColorCfM(@This JXO self, @Arg(0) double r, @Arg(1) double g, @Arg(2) double b, @Arg(3) double a) {
        InkGuardLogic.color(self, r, g, b, a);
    }

    @Hook(id = "jxo-pattern", target = JXO.class, method = "cfM(Lcom/bitwig/graphics/Ln2;)V", at = At.ENTRY)
    public static void onPattern(@This JXO self, @Arg(0) Ln2 pattern) {
        InkGuardLogic.pattern(self, pattern);
    }

    @Hook(id = "jxo-rule", target = JXO.class, method = "cfM(Lcom/bitwig/graphics/LWw;)V", at = At.ENTRY)
    public static void onRule(@This JXO self, @Arg(0) LWw rule) {
        InkGuardLogic.rule(self, rule);
    }

    @Hook(id = "jxo-rect", target = JXO.class, method = "MRY(DDDD)V", at = At.ENTRY)
    public static void onRect(@This JXO self, @Arg(0) double x, @Arg(1) double y, @Arg(2) double w, @Arg(3) double h) {
        InkGuardLogic.rect(self, x, y, w, h);
    }

    @Hook(id = "jxo-roundrect", target = JXO.class, method = "cfM(DDDDDLjava/util/EnumSet;)V", at = At.ENTRY)
    public static void onRoundRect(@This JXO self, @Arg(0) double x, @Arg(1) double y, @Arg(2) double w, @Arg(3) double h,
                                   @Arg(4) double radius, @Arg(5) EnumSet<?> corners) {
        InkGuardLogic.rect(self, x, y, w, h);
    }

    @Hook(id = "jxo-moveto", target = JXO.class, method = "MRY(DD)V", at = At.ENTRY)
    public static void onMoveTo(@This JXO self, @Arg(0) double x, @Arg(1) double y) {
        InkGuardLogic.point(self, x, y);
    }

    @Hook(id = "jxo-lineto", target = JXO.class, method = "cfM(DD)V", at = At.ENTRY)
    public static void onLineTo(@This JXO self, @Arg(0) double x, @Arg(1) double y) {
        InkGuardLogic.point(self, x, y);
    }

    @Hook(id = "jxo-arc-cw", target = JXO.class, method = "cfM(DDDDD)V", at = At.ENTRY)
    public static void onArcCw(@This JXO self, @Arg(0) double cx, @Arg(1) double cy, @Arg(2) double r) {
        InkGuardLogic.arc(self, cx, cy, r);
    }

    @Hook(id = "jxo-arc-ccw", target = JXO.class, method = "MRY(DDDDD)V", at = At.ENTRY)
    public static void onArcCcw(@This JXO self, @Arg(0) double cx, @Arg(1) double cy, @Arg(2) double r) {
        InkGuardLogic.arc(self, cx, cy, r);
    }

    @Hook(id = "jxo-curve", target = JXO.class, method = "cfM(DDDDDD)V", at = At.ENTRY)
    public static void onCurve(@This JXO self, @Arg(0) double x1, @Arg(1) double y1,
                               @Arg(2) double x2, @Arg(3) double y2, @Arg(4) double x3, @Arg(5) double y3) {
        InkGuardLogic.curve(self, x1, y1, x2, y2, x3, y3);
    }

    @Hook(id = "jxo-newpath", target = JXO.class, method = "nVd()V", at = At.ENTRY)
    public static void onNewPath(@This JXO self) {
        InkGuardLogic.pathClear(self);
    }

    @Hook(id = "jxo-stroke", target = JXO.class, method = "hbP()V", at = At.ENTRY)
    public static void onStroke(@This JXO self) {
        InkGuardLogic.pathClear(self);
    }

    @Hook(id = "jxo-clip", target = JXO.class, method = "lYc()V", at = At.ENTRY)
    public static void onClip(@This JXO self) {
        InkGuardLogic.pathClear(self);
    }

    @Hook(id = "jxo-fill-clear", target = JXO.class, method = "wGc()V", at = At.ENTRY)
    public static void onFillClear(@This JXO self) {
        InkGuardLogic.fill(self, true);
    }

    @Hook(id = "jxo-fill-keep", target = JXO.class, method = "eLY()V", at = At.ENTRY)
    public static void onFillKeep(@This JXO self) {
        InkGuardLogic.fill(self, false);
    }

    @Hook(id = "jxo-fillrect", target = JXO.class, method = "cfM(DDDDI)V", at = At.ENTRY)
    public static void onFillRect(@This JXO self, @Arg(0) double x, @Arg(1) double y,
                                  @Arg(2) double w, @Arg(3) double h, @Arg(4) int argb) {
        InkGuardLogic.fillRect(self, x, y, w, h, argb);
    }

    @Hook(id = "jxo-image-opts", target = JXO.class, method = "cfM(Lcom/bitwig/graphics/qc1;DDLcom/bitwig/graphics/CHR;)V", at = At.ENTRY)
    public static void onImageOpts(@This JXO self, @Arg(0) qc1 img, @Arg(1) double x, @Arg(2) double y) {
        InkGuardLogic.image(self, img, x, y);
    }

    @Hook(id = "jxo-image-alpha", target = JXO.class, method = "cfM(Lcom/bitwig/graphics/qc1;DDD)V", at = At.ENTRY)
    public static void onImageAlpha(@This JXO self, @Arg(0) qc1 img, @Arg(1) double x, @Arg(2) double y) {
        InkGuardLogic.image(self, img, x, y);
    }

    @Hook(id = "jxo-icon", target = JXO.class, method = "cfM(Lcom/bitwig/graphics/pHd;DD)V", at = At.ENTRY)
    public static void onIcon(@This JXO self, @Arg(0) pHd img, @Arg(1) double x, @Arg(2) double y) {
        InkGuardLogic.icon(self, img, x, y);
    }

    @Hook(id = "jxo-text-direct", target = JXO.class, method = "cfM(DDLjava/lang/String;Liok;)V",
          at = At.REPLACE_CALL, callOwner = JXO.class, call = "MRY(DDLjava/lang/String;Liok;)V", occurrence = 1)
    public static void onTextDirect(JXO self, double x, double y, String text, iok color) {
        iok adjusted = InkGuardLogic.ink(self, x, y, text, color);
        self.MRY(x, y, text, adjusted);
    }

    @Hook(id = "jxo-text-delegate", target = JXO.class, method = "cfM(DDLjava/lang/String;Liok;)V",
          at = At.REPLACE_CALL, callOwner = Dgc.class, call = "cfM(Lcom/bitwig/graphics/render_commands/JXO;DDLjava/lang/String;Liok;)V", occurrence = 1)
    public static void onTextDelegate(Dgc delegate, JXO self, double x, double y, String text, iok color) {
        iok adjusted = InkGuardLogic.ink(self, x, y, text, color);
        delegate.cfM(self, x, y, text, adjusted);
    }

    @Hook(id = "jxo-text-flag-direct", target = JXO.class, method = "cfM(DDLjava/lang/String;Liok;Z)V",
          at = At.REPLACE_CALL, callOwner = JXO.class, call = "MRY(DDLjava/lang/String;Liok;Z)V", occurrence = 1)
    public static void onTextFlagDirect(JXO self, double x, double y, String text, iok color, boolean flag) {
        iok adjusted = InkGuardLogic.ink(self, x, y, text, color);
        self.MRY(x, y, text, adjusted, flag);
    }

    @Hook(id = "jxo-text-flag-delegate", target = JXO.class, method = "cfM(DDLjava/lang/String;Liok;Z)V",
          at = At.REPLACE_CALL, callOwner = Dgc.class, call = "cfM(Lcom/bitwig/graphics/render_commands/JXO;DDLjava/lang/String;Liok;Z)V", occurrence = 1)
    public static void onTextFlagDelegate(Dgc delegate, JXO self, double x, double y, String text, iok color, boolean flag) {
        iok adjusted = InkGuardLogic.ink(self, x, y, text, color);
        delegate.cfM(self, x, y, text, adjusted, flag);
    }

    @Hook(id = "uev-gradient-stop", target = Uev.class, method = "cfM(DLiok;)V", at = At.ENTRY)
    public static void onGradientStop(@This Uev self, @Arg(1) iok color) {
        InkGuardLogic.stop(self, color);
    }
}
