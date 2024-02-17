package org.union4dev.deobfuscator.util;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public class ClassUtil {

    public static Method getMethod(Class<?> clz, String name, String desc) {
        for (Method method : clz.getDeclaredMethods()) {
            if (method.getName().equals(name) && Type.getMethodDescriptor(method).equals(desc)) {
                return method;
            }
        }
        return null;
    }
}
