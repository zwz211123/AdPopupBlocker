package dev.zwz.pipedreamadblocker;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

final class ReflectionUtils {
    private ReflectionUtils() {}

    static List<Method> declaredMethodsNamed(Class<?> clazz, String name) {
        List<Method> out = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                try { method.setAccessible(true); } catch (Throwable ignored) {}
                out.add(method);
            }
        }
        return out;
    }

    static Object defaultReturnValue(Executable executable) {
        if (!(executable instanceof Method method)) return null;
        Class<?> type = method.getReturnType();
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }

    static Object invokeBestEffortNoArg(Class<?> owner, Object receiver, String methodName) {
        for (Method method : declaredMethodsNamed(owner, methodName)) {
            if (method.getParameterCount() != 0) continue;
            try {
                Object target = Modifier.isStatic(method.getModifiers()) ? null : receiver;
                if (!Modifier.isStatic(method.getModifiers()) && target == null) continue;
                return method.invoke(target);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    static Object findFieldAssignableTo(Object owner, Class<?> wantedType) {
        if (owner == null) return null;
        for (Class<?> c = owner.getClass(); c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(owner);
                    if (value != null && wantedType.isAssignableFrom(value.getClass())) return value;
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }
}
