package org.union4dev.deobfuscator.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;

public abstract class Transformer implements Opcodes {

    public abstract void transform(Map<String, ClassNode> nodeMap);
}
