package com.graphitelight.inkguard;

import bml.api.AddField;
import com.bitwig.graphics.render_commands.JXO; // render command recorder
import com.bitwig.graphics.render_commands.Uev; // gradient pattern builder
import com.bitwig.graphics.render_commands.gJi; // cloneable graphics state stack entry

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Added fields on Bitwig's render-command classes. Kept in its own holder class so loading the
 * hook class does not force early initialization of Bitwig's graphics classes.
 */
final class Fields {
    // Recorder-owned surface list + pending path bbox (object reference; dies with JXO)
    @AddField(target = JXO.class, name = "graphite_ink_guard$state", type = Object.class)
    static final VarHandle RECORDER_STATE = handle(JXO.class, "graphite_ink_guard$state", Object.class);

    // Gradient stop range tracker (object reference; dies with Uev)
    @AddField(target = Uev.class, name = "graphite_ink_guard$grad", type = Object.class)
    static final VarHandle GRAD_STATE = handle(Uev.class, "graphite_ink_guard$grad", Object.class);

    // Graphics state stack fields on gJi (primitives so gJi.clone() copies them automatically)
    @AddField(target = gJi.class, name = "graphite_ink_guard$tx", type = double.class)
    static final VarHandle TX = handle(gJi.class, "graphite_ink_guard$tx", double.class);

    @AddField(target = gJi.class, name = "graphite_ink_guard$ty", type = double.class)
    static final VarHandle TY = handle(gJi.class, "graphite_ink_guard$ty", double.class);

    @AddField(target = gJi.class, name = "graphite_ink_guard$sx", type = double.class)
    static final VarHandle SX = handle(gJi.class, "graphite_ink_guard$sx", double.class);

    @AddField(target = gJi.class, name = "graphite_ink_guard$sy", type = double.class)
    static final VarHandle SY = handle(gJi.class, "graphite_ink_guard$sy", double.class);

    @AddField(target = gJi.class, name = "graphite_ink_guard$rot", type = double.class)
    static final VarHandle ROT = handle(gJi.class, "graphite_ink_guard$rot", double.class);

    @AddField(target = gJi.class, name = "graphite_ink_guard$argb", type = int.class)
    static final VarHandle ARGB = handle(gJi.class, "graphite_ink_guard$argb", int.class);

    @AddField(target = gJi.class, name = "graphite_ink_guard$solid", type = boolean.class)
    static final VarHandle SOLID = handle(gJi.class, "graphite_ink_guard$solid", boolean.class);

    @AddField(target = gJi.class, name = "graphite_ink_guard$evenOdd", type = boolean.class)
    static final VarHandle EVEN_ODD = handle(gJi.class, "graphite_ink_guard$evenOdd", boolean.class);

    private Fields() {}

    private static VarHandle handle(Class<?> owner, String name, Class<?> type) {
        try {
            return MethodHandles.privateLookupIn(owner, MethodHandles.lookup()).findVarHandle(owner, name, type);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
