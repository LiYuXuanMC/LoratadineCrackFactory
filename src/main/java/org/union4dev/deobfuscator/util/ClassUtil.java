package org.union4dev.deobfuscator.util;

import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class ClassUtil {
    public static byte[] getClassBytes(Class<?> klass) {
        String classLocation = klass.getName().replace(".", "/") + ".class";
        try (InputStream classStream = ClassUtil.class.getClassLoader().getResourceAsStream(classLocation)) {
            if (classStream == null) {
                return null;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int read;

            while((read = classStream.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Method getMethod(Class<?> clz, String name, String desc) {
        for (Method method : clz.getDeclaredMethods()) {
            if (method.getName().equals(name) && Type.getMethodDescriptor(method).equals(desc)) {
                return method;
            }
        }
        return null;
    }
}
