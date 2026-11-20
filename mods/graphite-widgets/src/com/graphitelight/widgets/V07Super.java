package com.graphitelight.widgets;

import bitwig.obf.aoH; // outer chooser panel (6.1.8: aoV)
import bitwig.obf.aoI; // its superclass
import bitwig.obf.dNN; // enum/value field (6.1.8: dPm)
import bitwig.obf.dNP; // its superclass
import com.bitwig.graphics.SYo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Inherited c_(SYo) paint of dNN and aoH, for the v07n17 disabled-look overrides. */
final class V07Super {
    private V07Super() {}

    static final MethodHandle DNN_PAINT = find(dNN.class, dNP.class);
    static final MethodHandle AOH_PAINT = find(aoH.class, aoI.class);

    private static MethodHandle find(Class<?> self, Class<?> parent) {
        try {
            return MethodHandles.privateLookupIn(self, MethodHandles.lookup())
                .findSpecial(parent, "c_", MethodType.methodType(void.class, SYo.class), self);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
