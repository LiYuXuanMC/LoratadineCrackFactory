package org.union4dev.deobfuscator.transformer.implement;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.Deobfuscator;
import org.union4dev.deobfuscator.execution.Execution;
import org.union4dev.deobfuscator.transformer.Transformer;
import org.union4dev.deobfuscator.util.ClassNodeUtil;
import org.union4dev.deobfuscator.util.ClassUtil;
import org.union4dev.deobfuscator.util.InstructionModifier;
import org.union4dev.deobfuscator.util.TransformerHelper;

import java.lang.reflect.Method;
import java.util.Map;

public class StringWrapperTransformer extends Transformer {

    // Logic breakable.
    @Override
    public void transform(Map<String, ClassNode> nodeMap) {
        Logger.warn("You are using a logic breakable transformer.");
        Logger.info("Start StringWrapperTransformer.");
        int count = 0;
        for (ClassNode classNode : nodeMap.values()) {
            for (MethodNode method : classNode.methods) {
                if (!isDecrypt(method))
                    continue;
                try {
                    final Frame<SourceValue>[] frames = new Analyzer<>(new SourceInterpreter()).analyze(classNode.name, method);
                    final InstructionModifier modifier = new InstructionModifier();
                    for (AbstractInsnNode instruction : method.instructions) {
                        if (instruction.getOpcode() != INVOKESTATIC)
                            continue;
                        final MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                        if (!methodInsnNode.desc.equals("(Ljava/lang/String;)Ljava/lang/String;"))
                            continue;

                        final Frame<SourceValue> frame = frames[method.instructions.indexOf(methodInsnNode)];
                        if (frame.getStack(frame.getStackSize() - 1).insns.size() != 1) { // ldc
                            continue;
                        }
                        final AbstractInsnNode abstractInsnNode = frame.getStack(frame.getStackSize() - 1).insns.iterator().next();

                        if (!ClassNodeUtil.isString(abstractInsnNode))
                            continue;

                        final LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractInsnNode;
                        final String encrypted = (String) ldcInsnNode.cst;

                        final Class<?> load = Execution.load(Deobfuscator.INSTANCE.getNodeFromClasspath(methodInsnNode.owner));
                        final Method decryptMethod = ClassUtil.getMethod(load, methodInsnNode.name, methodInsnNode.desc);
                        if (decryptMethod == null)
                            throw new RuntimeException("Failed to trace decrypt method for " + encrypted);
                        decryptMethod.setAccessible(true);

                        ldcInsnNode.cst = decryptMethod.invoke(null, encrypted);
                        modifier.remove(instruction);
                    }
                } catch (Throwable t) {
                    Logger.error(t);
                }
            }
        }
        Logger.info("Finish StringWrapperTransformer with " + count + " strings decrypted.");
    }

    private boolean isDecrypt(MethodNode methodNode) {
        if (TransformerHelper.countOf(methodNode, GETFIELD) > 0)
            return false;

        if (TransformerHelper.countOf(methodNode, GETSTATIC) > 0)
            return false;

        if (TransformerHelper.countOf(methodNode, PUTSTATIC) > 0)
            return false;

        if (TransformerHelper.countOf(methodNode, PUTFIELD) > 0)
            return false;

        return TransformerHelper.countOf(methodNode, Opcodes.INVOKESTATIC) <= 0;
    }
}
