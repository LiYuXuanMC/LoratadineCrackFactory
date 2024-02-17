package org.union4dev.deobfuscator.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.union4dev.deobfuscator.Deobfuscator;

import java.util.*;

public class SuperClassWriter extends ClassWriter {

    public SuperClassWriter(int flags) {
        super(flags);
    }

    public SuperClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (type1.equals("java/lang/Object") || type2.equals("java/lang/Object")) {
            return "java/lang/Object";
        }

        String first = getCpCommonSuperClass(type1, type2);
        String second = getCpCommonSuperClass(type2, type1);

        if (!"java/lang/Object".equals(first)) {
            return first;
        }

        if (!"java/lang/Object".equals(second)) {
            return second;
        }

        return getCommonSuperClass(Deobfuscator.INSTANCE.getNodeFromClasspath(type1).superName,
                Deobfuscator.INSTANCE.getNodeFromClasspath(type1).superName);
    }

    private String getCpCommonSuperClass(String type1, String type2) {
        ClassNode first = Deobfuscator.INSTANCE.getNodeFromClasspath(type1);
        final ClassNode second = Deobfuscator.INSTANCE.getNodeFromClasspath(type2);

        if (isAssignableFrom(type1, type2)) {
            return type1;
        } else if (isAssignableFrom(type2, type1)) {
            return type2;
        } else if (((first.access & Opcodes.ACC_INTERFACE) == 0) || ((second.access & Opcodes.ACC_INTERFACE) == 0)) {
            return "java/lang/Object";
        } else {
            while (!isAssignableFrom(type1, type2)) {
                type1 = first.superName;
                first = Deobfuscator.INSTANCE.getNodeFromClasspath(type1);
            }
            return type1;
        }
    }

    private boolean isAssignableFrom(String type1, String type2) {
        if ("java/lang/Object".equals(type1)) {
            return true;
        }

        if (type1.equals(type2)) {
            return true;
        }

        Deobfuscator.INSTANCE.getNodeFromClasspath(type1);
        Deobfuscator.INSTANCE.getNodeFromClasspath(type2);

        final HierarchyClass hierarchyClass = Deobfuscator.INSTANCE.getHierarchyClass(type1);

        final List<String> childList = new ArrayList<>();
        final Queue<String> processQueue = new ArrayDeque<>();
        hierarchyClass.child.forEach(child -> processQueue.add(child.name));

        while (!processQueue.isEmpty()) {
            final String next = processQueue.poll();
            if (!childList.contains(next)) {
                childList.add(next);
                Deobfuscator.INSTANCE.getNodeFromClasspath(next);
                final HierarchyClass temp = Deobfuscator.INSTANCE.getHierarchyClass(next);
                temp.child.forEach(child -> processQueue.add(child.name));
            }
        }

        return childList.contains(type2);
    }
}
