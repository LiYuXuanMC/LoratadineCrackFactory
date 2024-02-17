package org.union4dev.deobfuscator.asm;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

public class HierarchyClass {
    public ClassNode classNode;

    public List<ClassNode> parents = new ArrayList<>();
    public List<ClassNode> child = new ArrayList<>();

    public HierarchyClass(ClassNode classNode) {
        this.classNode = classNode;
    }
}
