package org.union4dev.deobfuscator.execution;

import org.objectweb.asm.tree.ClassNode;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.Deobfuscator;
import org.union4dev.deobfuscator.util.ClassNodeUtil;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class ExecutionClassLoader extends ClassLoader {

    private final Map<String, Class<?>> loaded = new HashMap<>();
    private final boolean ignore;

    public ExecutionClassLoader(ClassLoader parent, boolean ignore) {
        super(parent);
        this.ignore = ignore;
    }

    public Class<?> get(String name, byte[] bytes) {
        if (loaded.containsKey(name)) return loaded.get(name);
        try {
            final Method define = ClassLoader.class.getDeclaredMethod("defineClass0", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
            define.setAccessible(true);
            final Class<?> c = (Class<?>) define.invoke(this, name, bytes, 0, bytes.length, null);
            resolveClass(c);
            loaded.put(name, c);
            return c;
        } catch (Exception ignored) {
            try {
                final Class<?> c = defineClass(name, bytes, 0, bytes.length);
                resolveClass(c);
                loaded.put(name, c);
                return c;
            } catch (Throwable throwable) {
                Logger.error(throwable);
                return null;
            }
        }
    }

    @Override
    public Class<?> findClass(String name) {
        if (this.loaded.containsKey(name)) {
            return this.loaded.get(name);
        } else if (Deobfuscator.INSTANCE.getClasspathMap().containsKey(name.replace(".", "/"))) {
            return get(name, ClassNodeUtil.parseNode(Deobfuscator.INSTANCE.getClasspathMap().get(name.replace(".", "/"))));
        } else {
            if (ignore) {
                final ClassNode gen = new ClassNode();
                gen.name = name.replace('.', '/');
                gen.superName = "java/lang/Object";
                gen.version = 52;
                return get(name, ClassNodeUtil.parseNode(gen));
            } else {
                throw new RuntimeException("Failed to find class " + name + ".");
            }
        }
    }
}
