package org.union4dev.deobfuscator.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// https://github.com/java-deobfuscator/deobfuscator/blob/master/src/main/java/com/javadeobfuscator/deobfuscator/utils/TransformerHelper.java
public class TransformerHelper implements Opcodes {

    public static boolean isInvokeVirtual(AbstractInsnNode insn, String owner, String name, String desc) {
        if (insn == null) {
            return false;
        }
        if (insn.getOpcode() != INVOKEVIRTUAL) {
            return false;
        }
        final MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
        return (owner == null || methodInsnNode.owner.equals(owner)) &&
                (name == null || methodInsnNode.name.equals(name)) &&
                (desc == null || methodInsnNode.desc.equals(desc));
    }

    public static boolean isInvokeSpecial(AbstractInsnNode insn, String owner, String name, String desc) {
        if (insn == null) {
            return false;
        }
        if (insn.getOpcode() != INVOKESPECIAL) {
            return false;
        }
        final MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
        return (owner == null || methodInsnNode.owner.equals(owner)) &&
                (name == null || methodInsnNode.name.equals(name)) &&
                (desc == null || methodInsnNode.desc.equals(desc));
    }

    public static boolean isInvokeStatic(AbstractInsnNode insn, String owner, String name, String desc) {
        if (insn == null) {
            return false;
        }
        if (insn.getOpcode() != INVOKESTATIC) {
            return false;
        }
        final MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
        return (owner == null || methodInsnNode.owner.equals(owner)) &&
                (name == null || methodInsnNode.name.equals(name)) &&
                (desc == null || methodInsnNode.desc.equals(desc));
    }

    public static boolean isInvokeDynamic(AbstractInsnNode insn, String name, String desc, String bsmOwner, String bsmName, String bsmDesc, Integer bsmArgAmount) {
        if (insn == null) {
            return false;
        }
        if (insn.getOpcode() != INVOKEDYNAMIC) {
            return false;
        }
        final InvokeDynamicInsnNode methodInsnNode = (InvokeDynamicInsnNode) insn;
        return (name == null || methodInsnNode.name.equals(name)) &&
                (desc == null || methodInsnNode.desc.equals(desc)) &&
                (bsmOwner == null || methodInsnNode.bsm.getOwner().equals(bsmOwner)) &&
                (bsmName == null || methodInsnNode.bsm.getName().equals(bsmName)) &&
                (bsmDesc == null || methodInsnNode.bsm.getDesc().equals(bsmDesc)) &&
                (bsmArgAmount == null || methodInsnNode.bsmArgs.length == bsmArgAmount);
    }

    public static boolean isPutStatic(AbstractInsnNode insn, String owner, String name, String desc) {
        if (insn == null) {
            return false;
        }
        if (insn.getOpcode() != PUTSTATIC) {
            return false;
        }
        final FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
        return (owner == null || fieldInsnNode.owner.equals(owner)) &&
                (name == null || fieldInsnNode.name.equals(name)) &&
                (desc == null || fieldInsnNode.desc.equals(desc));
    }

    public static boolean isGetStatic(AbstractInsnNode insn, String owner, String name, String desc) {
        if (insn == null) {
            return false;
        }
        if (insn.getOpcode() != GETSTATIC) {
            return false;
        }
        final FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
        return (owner == null || fieldInsnNode.owner.equals(owner)) &&
                (name == null || fieldInsnNode.name.equals(name)) &&
                (desc == null || fieldInsnNode.desc.equals(desc));
    }

    public static boolean containsInvokeStatic(MethodNode methodNode, String owner, String name, String desc) {
        for (AbstractInsnNode insn : methodNode.instructions) {
            if (isInvokeStatic(insn, owner, name, desc)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsInvokeVirtual(MethodNode methodNode, String owner, String name, String desc) {
        for (AbstractInsnNode insn : methodNode.instructions) {
            if (isInvokeVirtual(insn, owner, name, desc)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsInvokeSpecial(MethodNode methodNode, String owner, String name, String desc) {
        for (AbstractInsnNode insn : methodNode.instructions) {
            if (isInvokeSpecial(insn, owner, name, desc)) {
                return true;
            }
        }
        return false;
    }

    public static int countOf(MethodNode methodNode, int opcode) {
        int i = 0;
        for (AbstractInsnNode insnNode : methodNode.instructions) {
            if (insnNode.getOpcode() == opcode) {
                i++;
            }
        }
        return i;
    }
}
