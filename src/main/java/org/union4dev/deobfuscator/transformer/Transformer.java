package org.union4dev.deobfuscator.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.union4dev.deobfuscator.Deobfuscator;

import java.util.Map;

public abstract class Transformer implements Opcodes {

    public abstract void transform(Map<String, ClassNode> nodeMap);

    public void loadSecurityChecker() {
        Deobfuscator.INSTANCE.loadSecurityChecker();
    }

    public void resetSecurityChecker() {
        Deobfuscator.INSTANCE.resetSecurityChecker();
    }
}
