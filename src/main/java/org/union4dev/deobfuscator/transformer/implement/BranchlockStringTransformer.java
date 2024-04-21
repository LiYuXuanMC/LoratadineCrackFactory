package org.union4dev.deobfuscator.transformer.implement;

import org.objectweb.asm.tree.*;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.execution.Execution;
import org.union4dev.deobfuscator.transformer.Transformer;
import org.union4dev.deobfuscator.util.ClassNodeUtil;
import org.union4dev.deobfuscator.util.InstructionModifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BranchlockStringTransformer extends Transformer {

    /*
    getstatic test/Main.a [Ljava/lang/String;
    [num]
    (cast)
    [num]
    (cast)
    ishl / iushr
    aaload
     */

    @Override
    public void transform(Map<String, ClassNode> nodeMap) {
        Logger.info("Start BranchlockStringTransformer.");
        int count = 0;
        for (ClassNode classNode : nodeMap.values()) {
            try {
                final MethodNode method = ClassNodeUtil.getMethod(classNode, "<clinit>", "()V");

                if (method == null)
                    continue;

                /*
                 label
                 aload
                 iload
                 iinc
                 new java/lang/String
                 dup
                 aload
                 invokespecial java/lang/String.<init> ([C)V
                 invokevirtual java/lang/String.intern ()Ljava/lang/String;
                 aastore
                 iload
                 aload
                 arraylength
                 if_icmplt
                 aload
                 putstatic
                 */

                final Class<?> load = Execution.load(classNode);
                Field field = null;
                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof LabelNode && instruction.getNext() != null && instruction.getNext().getOpcode() == ALOAD
                            && instruction.getNext().getNext() != null && instruction.getNext().getNext().getOpcode() == ILOAD
                            && instruction.getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getOpcode() == IINC
                            && instruction.getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getOpcode() == NEW
                            && instruction.getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getOpcode() == DUP
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == ALOAD
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == INVOKESPECIAL
                            && ((MethodInsnNode) instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext()).owner.equals("java/lang/String")
                            && ((MethodInsnNode) instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext()).name.equals("<init>")
                            && ((MethodInsnNode) instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext()).desc.equals("([C)V")
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == INVOKEVIRTUAL
                            && ((MethodInsnNode) instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext()).owner.equals("java/lang/String")
                            && ((MethodInsnNode) instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext()).name.equals("intern")
                            && ((MethodInsnNode) instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext()).desc.equals("()Ljava/lang/String;")
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == AASTORE
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == ILOAD
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == ALOAD
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == ARRAYLENGTH
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == IF_ICMPLT
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == ALOAD
                            && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == PUTSTATIC) {
                        final FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext().getNext();
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[Ljava/lang/String;")) {
                            field = load.getDeclaredField(fieldInsnNode.name);
                            break;
                        }
                    }
                }

                if (field == null)
                    continue;

                field.setAccessible(true);
                final String[] strings = (String[]) field.get(null);

                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("<clinit>")) continue;

                    final InstructionModifier modifier = new InstructionModifier();
                    for (AbstractInsnNode instruction : methodNode.instructions) {
                        if (instruction.getOpcode() == GETSTATIC && ((FieldInsnNode) instruction).owner.equals(classNode.name) && ((FieldInsnNode) instruction).name.equals(field.getName()) && ((FieldInsnNode) instruction).desc.equals("[Ljava/lang/String;")) {
                            int index;
                            final List<Number> twoNum = new ArrayList<>();
                            final List<AbstractInsnNode> cut = new ArrayList<>();
                            AbstractInsnNode current = instruction.getNext();
                            int opcode = 0;
                            while (current != null && current.getOpcode() != AALOAD) {
                                if (ClassNodeUtil.isNumber(current))
                                    twoNum.add(ClassNodeUtil.getNumber(current));
                                if (current.getOpcode() == ISHL || current.getOpcode() == IUSHR)
                                    opcode = current.getOpcode();
                                cut.add(current);
                                current = current.getNext();
                            }
                            cut.add(current);

                            if (opcode == ISHL) {
                                index = (int) twoNum.get(0) << (int) twoNum.get(1);
                            } else if (opcode == IUSHR) {
                                index = (int) twoNum.get(0) >>> (int) twoNum.get(1);
                            } else {
                                throw new IllegalStateException("Unknown opcode");
                            }

                            count++;
                            modifier.replace(instruction, new LdcInsnNode(strings[index]));
                            modifier.removeAll(cut);
                        }
                    }
                    modifier.apply(methodNode);
                }
            } catch (Throwable t) {
                Logger.error(t);
            }
        }
        Logger.info("Finish BranchlockStringTransformer with " + count + " strings decrypted.");
    }
}
