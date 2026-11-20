package com.graphitelight.devices;

import bitwig.obf.iok; // ARGB colour value
import com.bitwig.base.serial.CuM; // event source interface
import com.bitwig.base.serial.MVg; // serialized class descriptor
import com.bitwig.base.serial.Wkj; // serialized event sink interface
import com.bitwig.base.serial.WzZ; // serialized property descriptor (MRY() = property id)

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

/**
 * Streaming Bitwig serialization sink that tracks the numeric property path (matching
 * tools/port/DevicePort.java) and applies matching scalar Byte/Double edits on the fly.
 */
final class EditingSink implements Wkj {
    record Edit(String expected, String replacement) {}

    private enum Kind { OBJECT, PROP, LIST, MAP, MAP_ENTRY, REF, REF_TARGET }

    private static final class Frame {
        final Kind kind;
        final String path;
        int index;
        Frame(Kind kind, String path) {
            this.kind = kind;
            this.path = path;
        }
    }

    private final Wkj delegate;
    private final Map<String, Edit> edits;
    private final Deque<Frame> stack = new ArrayDeque<>();
    int applied;
    int mismatched;

    EditingSink(Wkj delegate, Map<String, Edit> edits) {
        this.delegate = delegate;
        this.edits = edits;
    }

    /** Compute the path of the value starting now and advance any enclosing list/map/ref frame. */
    private String enterValue() {
        Frame top = stack.peek();
        if (top == null) return "";
        return switch (top.kind) {
            case PROP -> top.path;
            case LIST -> top.path + "/" + (top.index++);
            case MAP_ENTRY -> {
                stack.pop();
                yield top.path;
            }
            case REF -> {
                String p = top.path + "/@target";
                stack.push(new Frame(Kind.REF_TARGET, p));
                yield p;
            }
            default -> top.path;
        };
    }

    private void finishRefTargetIfNeeded() {
        Frame top = stack.peek();
        if (top != null && top.kind == Kind.REF_TARGET) {
            stack.pop();
        }
    }

    static final class SourceAdapter implements CuM {
        private final CuM source;
        private final Map<String, Edit> edits;
        int applied;
        int mismatched;

        SourceAdapter(CuM source, Map<String, Edit> edits) {
            this.source = source;
            this.edits = edits;
        }

        @Override
        public void cfM(Wkj targetSink) {
            EditingSink editing = new EditingSink(targetSink, edits);
            source.cfM(editing);
            this.applied = editing.applied;
            this.mismatched = editing.mismatched;
        }
    }

    /* ---------- container events ---------- */

    @Override
    public void cfM(MVg classDesc) {
        String p = enterValue();
        stack.push(new Frame(Kind.OBJECT, p));
        delegate.cfM(classDesc);
    }

    @Override
    public void IwV() { // objectEnd
        if (!stack.isEmpty() && stack.peek().kind == Kind.OBJECT) stack.pop();
        finishRefTargetIfNeeded();
        delegate.IwV();
    }

    @Override
    public void cfM(WzZ propDesc) { // propertyStart
        String base = stack.isEmpty() ? "" : stack.peek().path;
        int id = propDesc.MRY(); // numeric property id
        stack.push(new Frame(Kind.PROP, base + "/" + id));
        delegate.cfM(propDesc);
    }

    @Override
    public void hUR() { // propertyEnd
        if (!stack.isEmpty() && stack.peek().kind == Kind.PROP) stack.pop();
        delegate.hUR();
    }

    @Override
    public void TMF() { // listStart
        String p = enterValue();
        stack.push(new Frame(Kind.LIST, p));
        delegate.TMF();
    }

    @Override
    public void lYc() { // listEnd
        if (!stack.isEmpty() && stack.peek().kind == Kind.LIST) stack.pop();
        finishRefTargetIfNeeded();
        delegate.lYc();
    }

    @Override
    public void E3() { // mapStart
        String p = enterValue();
        stack.push(new Frame(Kind.MAP, p));
        delegate.E3();
    }

    @Override
    public void FzB(String key) { // mapKey
        Frame top = stack.peek();
        if (top != null && top.kind == Kind.MAP) {
            stack.push(new Frame(Kind.MAP_ENTRY, top.path + "/@map/" + (top.index++)));
        }
        delegate.FzB(key);
    }

    @Override
    public void wGc() { // mapEnd
        if (!stack.isEmpty() && stack.peek().kind == Kind.MAP) stack.pop();
        finishRefTargetIfNeeded();
        delegate.wGc();
    }

    @Override
    public void FzB() { // refStart
        String p = enterValue();
        stack.push(new Frame(Kind.REF, p));
        delegate.FzB();
    }

    @Override
    public void Hnt(String member) { // refMember
        finishRefTargetIfNeeded();
        if (!stack.isEmpty() && stack.peek().kind == Kind.REF) stack.pop();
        finishRefTargetIfNeeded();
        delegate.Hnt(member);
    }

    /* ---------- editable scalar events ---------- */

    @Override
    public void cfM(byte v) {
        String p = enterValue();
        Edit e = edits.get(p);
        if (e != null && e.expected.startsWith("Byte:") && e.replacement.startsWith("Byte:")) {
            byte exp = Byte.parseByte(e.expected.substring(5));
            if (v == exp) {
                v = Byte.parseByte(e.replacement.substring(5));
                applied++;
            } else {
                mismatched++;
            }
        }
        finishRefTargetIfNeeded();
        delegate.cfM(v);
    }

    @Override
    public void cfM(double v) {
        String p = enterValue();
        Edit e = edits.get(p);
        if (e != null && e.expected.startsWith("Double:") && e.replacement.startsWith("Double:")) {
            double exp = Double.parseDouble(e.expected.substring(7));
            if (Double.compare(v, exp) == 0) {
                v = Double.parseDouble(e.replacement.substring(7));
                applied++;
            } else {
                mismatched++;
            }
        }
        finishRefTargetIfNeeded();
        delegate.cfM(v);
    }

    /* ---------- pass-through scalar & token events ---------- */

    @Override public void cfM() { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(); }
    @Override public void MRY() { enterValue(); finishRefTargetIfNeeded(); delegate.MRY(); }
    @Override public boolean Hnt() { return delegate.Hnt(); }
    @Override public void cfM(Object v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(short v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(int v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(long v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(boolean v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(float v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(String v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void MRY(int v) { enterValue(); finishRefTargetIfNeeded(); delegate.MRY(v); }
    @Override public void MRY(String v) { enterValue(); finishRefTargetIfNeeded(); delegate.MRY(v); }
    @Override public void cfM(UUID v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(iok v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void UNj() { enterValue(); finishRefTargetIfNeeded(); delegate.UNj(); }
    @Override public void cfM(byte[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(short[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(int[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(long[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(float[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(double[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(String[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
    @Override public void cfM(boolean[] v) { enterValue(); finishRefTargetIfNeeded(); delegate.cfM(v); }
}
