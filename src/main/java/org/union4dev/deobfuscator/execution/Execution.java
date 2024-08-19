package org.union4dev.deobfuscator.execution;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class Execution {

    public static Class<?> load(ClassNode cn) {
        final ClassWriter classWriter = new ClassWriter(0);
        cn.accept(classWriter);
        return new ExecutionClassLoader(ClassLoader.getSystemClassLoader(), false).get(cn.name.replace("/", "."), classWriter.toByteArray());
    }

    public static Class<?> loadIgnoreImports(ClassNode cn) {
        final ClassWriter classWriter = new ClassWriter(0);
        cn.accept(classWriter);
        return new ExecutionClassLoader(ClassLoader.getSystemClassLoader(), true).get(cn.name.replace("/", "."), classWriter.toByteArray());
    }
}
