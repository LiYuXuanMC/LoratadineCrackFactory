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
import java.util.HashMap;
import java.util.Map;

public class AllatoriStringTransformer extends Transformer {

    /*
    ldc "xxxxxx"
    xxxxxxxxxxxxxxxxxx
    xxxxxxxxxxxxxxxxxx
    invokestatic xxx.ALLATORIxDEMO(Ljava/lang/String;)Ljava/lang/String;
     */

    @Override
    public void transform(Map<String, ClassNode> nodeMap) {
        Logger.info("Start AllatoriStringTransformer.");
        int count = 0;
        final Map<String, MethodNode> decryptFn = new HashMap<>();
        for (ClassNode classNode : nodeMap.values()) {
            for (MethodNode method : classNode.methods) {
                try {
                    final Frame<SourceValue>[] frames = new Analyzer<>(new SourceInterpreter()).analyze(classNode.name, method);
                    final InstructionModifier modifier = new InstructionModifier();
                    for (AbstractInsnNode instruction : method.instructions) {
                        if (instruction.getOpcode() != INVOKESTATIC)
                            continue;
                        final MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                        if (!methodInsnNode.name.equals("ALLATORIxDEMO") || (!methodInsnNode.desc.equals("(Ljava/lang/String;)Ljava/lang/String;") && !methodInsnNode.desc.equals("(Ljava/lang/Object;)Ljava/lang/String;")))
                            continue;
                        boolean isDecrypt = false;
                        if (decryptFn.containsKey(methodInsnNode.owner)) {
                            final MethodNode methodNode = decryptFn.get(methodInsnNode.owner);
                            if (methodInsnNode.name.equals(methodNode.name) && methodInsnNode.desc.equals(methodNode.desc)) {
                                isDecrypt = true;
                            } else {
                                if (checkMethod(methodInsnNode, decryptFn))
                                    isDecrypt = true;
                            }
                        } else if (checkMethod(methodInsnNode, decryptFn))
                            isDecrypt = true;

                        if (isDecrypt) {
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

                            loadSecurityChecker();
                            ldcInsnNode.cst = decryptMethod.invoke(null, encrypted);
                            resetSecurityChecker();
                            modifier.remove(instruction);
                            count++;
                        }
                    }
                    modifier.apply(method);
                } catch (Throwable t) {
                    Logger.error(t);
                }
            }
        }

        decryptFn.forEach((name, method) -> {
            final ClassNode nodeFromClasspath = Deobfuscator.INSTANCE.getNodeFromClasspath(name);
            nodeFromClasspath.methods.remove(method);
        });

        Logger.info("Finish AllatoriStringTransformer with " + count + " strings decrypted and " + decryptFn.size() + " decrypt methods removed.");
    }

    private boolean checkMethod(MethodInsnNode methodInsnNode, Map<String, MethodNode> decryptFn) {
        final ClassNode nodeFromClasspath = Deobfuscator.INSTANCE.getNodeFromClasspath(methodInsnNode.owner);
        final MethodNode uncheckedMethod = ClassNodeUtil.getMethod(nodeFromClasspath, methodInsnNode.name, methodInsnNode.desc);
        if (!isDecryptFn(uncheckedMethod))
            return false;
        decryptFn.put(methodInsnNode.owner, uncheckedMethod);
        return true;
    }

    private boolean isDecryptFn(MethodNode methodNode) {
        if (TransformerHelper.countOf(methodNode, Opcodes.NEWARRAY) == 0)
            return false;
        if (TransformerHelper.countOf(methodNode, Opcodes.IXOR) == 0)
            return false;
        if (!TransformerHelper.containsInvokeVirtual(methodNode, "java/lang/String", "charAt", "(I)C"))
            return false;
        return TransformerHelper.containsInvokeVirtual(methodNode, "java/lang/String", "length", "()I");
    }
}
