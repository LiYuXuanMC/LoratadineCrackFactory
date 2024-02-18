package org.union4dev.deobfuscator.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://github.com/java-deobfuscator/deobfuscator/blob/master/src/main/java/com/javadeobfuscator/deobfuscator/utils/InstructionModifier.java
public class InstructionModifier {

    private static final InsnList EMPTY_LIST = new InsnList();

    private final Map<AbstractInsnNode, InsnList> replacements = new HashMap<>();
    private final Map<AbstractInsnNode, InsnList> appends = new HashMap<>();
    private final Map<AbstractInsnNode, InsnList> prepends = new HashMap<>();

    public void append(AbstractInsnNode original, InsnList append) {
        appends.put(original, append);
    }

    public void prepend(AbstractInsnNode original, InsnList append) {
        prepends.put(original, append);
    }

    public void replace(AbstractInsnNode original, AbstractInsnNode... insns) {
        final InsnList singleton = new InsnList();
        for (AbstractInsnNode replacement : insns) {
            singleton.add(replacement);
        }
        replacements.put(original, singleton);
    }

    public void replace(AbstractInsnNode original, InsnList replacements) {
        this.replacements.put(original, replacements);
    }

    public void remove(AbstractInsnNode original) {
        replacements.put(original, EMPTY_LIST);
    }

    public void removeAll(List<AbstractInsnNode> toRemove) {
        for (AbstractInsnNode insn : toRemove) {
            remove(insn);
        }
    }

    public void apply(MethodNode methodNode) {
        replacements.forEach((insn, list) -> {
            methodNode.instructions.insert(insn, list);
            methodNode.instructions.remove(insn);
        });
        prepends.forEach((insn, list) -> methodNode.instructions.insertBefore(insn, list));
        appends.forEach((insn, list) -> methodNode.instructions.insert(insn, list));
    }
}
