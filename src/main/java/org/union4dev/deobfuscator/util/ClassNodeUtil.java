package org.union4dev.deobfuscator.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.asm.SuperClassWriter;

public class ClassNodeUtil {

    public static byte[] parseNode(ClassNode classNode) {
        ClassWriter writer = new SuperClassWriter(ClassWriter.COMPUTE_FRAMES);
        try {
            classNode.accept(writer);
        } catch (Throwable e) {
            if (e instanceof NegativeArraySizeException || e instanceof ArrayIndexOutOfBoundsException) {
                Logger.warn("Failed to compute frames while writing " + classNode.name + " COMPUTE_MAX instead.");
                writer = new SuperClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
            } else if (e.getMessage() != null) {
                if (e.getMessage().equals("JSR/RET are not supported with computeFrames option")) {
                    Logger.warn(classNode.name + " contained JSR/RET COMPUTE_MAXS instead.");
                    writer = new SuperClassWriter(ClassWriter.COMPUTE_MAXS);
                    classNode.accept(writer);
                } else {
                    Logger.warn("Error while writing " + classNode.name + " due to " + e.getMessage());
                }
            } else {
                Logger.warn("Error while writing " + classNode.name + " stacktrace here.");
                Logger.error(e);
            }
        }
        return writer.toByteArray();
    }

    public static byte[] quickParseNode(ClassNode classNode) {
        final ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public static ClassNode quickParseBytes(byte[] bytes) {
        final ClassReader classReader = new ClassReader(bytes);
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    public boolean isNumber(AbstractInsnNode node) {
        if(node.getOpcode() >= Opcodes.ICONST_M1 && node.getOpcode() <= Opcodes.ICONST_5)
            return true;
        if(node.getOpcode() == Opcodes.SIPUSH || node.getOpcode() == Opcodes.BIPUSH)
            return true;
        if(node instanceof LdcInsnNode) {
            final LdcInsnNode ldc = (LdcInsnNode)node;
            return ldc.cst instanceof Number;
        }
        return false;
    }

    public static MethodNode getMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        return null;
    }

    public static FieldNode getField(ClassNode classNode, String name, String desc) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.name.equals(name) && fieldNode.desc.equals(desc)) {
                return fieldNode;
            }
        }
        return null;
    }
}
