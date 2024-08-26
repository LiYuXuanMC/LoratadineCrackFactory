package org.union4dev.deobfuscator.transformer.implement;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.transformer.Transformer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrackTransformer_V1_2 extends Transformer {
    public static List<String> notModified = new ArrayList<>();
    @Override
    public void transform(Map<String, ClassNode> nodeMap) {
        Logger.info("Start Cracking...");
        notModified.addAll(nodeMap.keySet());
        ClassNode added2 = null;
        for (ClassNode value : nodeMap.values()) {
            if(value.name.equals("cn/lzq/injection/Fucker")){
                value.methods.removeIf(methodNode -> methodNode.name.equals("<clinit>"));
            }
            if(value.name.equals("cn/lzq/injection/InjectionEndpoint")){
                Logger.info("Found Endpoint Class {}",value.name);
                for (MethodNode method : value.methods) {
                    for (AbstractInsnNode instruction : method.instructions) {
                        if(instruction instanceof MethodInsnNode methodInsnNode){
                            if(methodInsnNode.getOpcode() == INVOKEVIRTUAL && methodInsnNode.desc.equals("()V")){
                                Logger.info("Found main class in {}",methodInsnNode.owner);
                                File header = new File("loader/loader/src/base/classes/obfuscation.h");
                                if(header.exists()){
                                    Logger.info("Found header in {}, deleting...",header.getAbsolutePath());
                                    try {
                                        Files.delete(header.toPath());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    notModified.remove(methodInsnNode.owner);
                                    String initClientMethod = "";
                                    ClassNode mainNode = nodeMap.get(methodInsnNode.owner);
                                    for (MethodNode methodNode : mainNode.methods) {
                                        if(methodNode.name.equals("<init>")){
                                            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                                                if(abstractInsnNode instanceof MethodInsnNode methodInsnNode1 && methodInsnNode1.getOpcode() == INVOKEVIRTUAL && methodInsnNode1.owner.equals(methodInsnNode.owner)){
                                                    String headerText = "const char* mian_klass = \""+ methodInsnNode.owner+"\";";
                                                    FileUtils.writeByteArrayToFile(header,headerText.getBytes(StandardCharsets.UTF_8));
                                                    Logger.info("Header created in {}.",header.getAbsolutePath());
                                                    initClientMethod = methodInsnNode1.name;
                                                }
                                            }
                                        }else {
                                            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                                                if(abstractInsnNode instanceof MethodInsnNode methodInsnNode1){
                                                    if(methodInsnNode1.owner.equals("cn/lzq/injection/Fucker")){
                                                        Logger.info("Removed auth {} method in {}",methodInsnNode1.name,methodNode.name);
                                                        methodNode.instructions.remove(methodInsnNode1);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    added2 = new ClassNode();
                                    String name = mainNode.name;
                                    added2.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", null);
                                    mainNode.methods.removeIf(methodNode -> methodNode.name.equals("<init>"));
                                    added2.methods.addAll(mainNode.methods);
                                    added2.fields.addAll(mainNode.fields);

                                    MethodNode methodVisitor = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
                                    methodVisitor.visitVarInsn(ALOAD, 0);
                                    methodVisitor.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>", "()V", false);
                                    methodVisitor.visitVarInsn(ALOAD, 0);
                                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL,name,initClientMethod, "()V", false);
                                    methodVisitor.visitInsn(RETURN);
                                    methodVisitor.visitMaxs(1,1);
                                    methodVisitor.visitEnd();

                                    added2.methods.add(methodVisitor);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }else {
                MethodNode toRemove = null;
                for (MethodNode method : value.methods) {
                    if (method.desc.endsWith(")Z")) {
                        for (AbstractInsnNode instruction : method.instructions) {
                            if(instruction instanceof MethodInsnNode methodInsnNode){
                                if(methodInsnNode.name.equals("exec")){
                                    Logger.info("Found execution method {}#{} {}, removing...",value.name,method.name,method.desc);
                                    toRemove = method;
                                    break;
                                }
                            }
                        }
                    }

                    if(method.name.equals("<init>") && method.desc.equals("(Lio/netty/channel/Channel;)V")){
                        Logger.info("Found ByteUtil Class {}",value.name);
                        notModified.remove(value.name);
                        for (MethodNode methodNode : value.methods) {
                            if(methodNode.desc.equals("()V") && !methodNode.name.equals("<init>")){
                                method.instructions.clear();
                                method.instructions.add(new InsnNode(RETURN));
                                Logger.info("Removed networking method {}#{} {}",value.name,methodNode.name,methodNode.desc);
                            }
                        }
                    }

                    if(method.desc.equals("(Lnet/minecraftforge/event/TickEvent$ClientTickEvent;)V")){
                        Logger.info("Found tick auth method in class {}.",value.name);
                        notModified.remove(value.name);
                        for (AbstractInsnNode instruction : method.instructions) {
                            if(instruction instanceof MethodInsnNode methodInsnNode){
                                if(methodInsnNode.getOpcode() == INVOKEVIRTUAL && methodInsnNode.owner.equals(value.name) && methodInsnNode.desc.equals("()V")){
                                    Logger.info("Found loaded ()V method invoke, removing auth..");
                                    method.instructions.insert(instruction,new InsnNode(RETURN));
                                    break;
                                }
                            }
                        }
                    }

                    for (AbstractInsnNode instruction : method.instructions) {
                        if(instruction instanceof MethodInsnNode methodInsnNode){
                            if(methodInsnNode.name.equals("exit")){
                                notModified.remove(value.name);
                                Logger.info("Found exit instruction in class {}",value.name);
                                method.instructions.remove(instruction.getPrevious());
                                method.instructions.remove(instruction);
                                break;
                            }
                        }
                    }
                }
                if(toRemove != null){
                    notModified.remove(value.name);
                    value.methods.remove(toRemove);

                    MethodNode methodVisitor = new MethodNode(toRemove.access,toRemove.name,toRemove.desc,null,null);
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitLineNumber(24, label0);
                    methodVisitor.visitInsn(ICONST_0);
                    methodVisitor.visitInsn(IRETURN);
                    Label label1 = new Label();
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitLocalVariable("sb", "Ljava/lang/String;", null, label0, label1, 0);
                    methodVisitor.visitMaxs(1, 1);
                    methodVisitor.visitEnd();
                    value.methods.add(methodVisitor);

                    Logger.info("Removed execution method for class {}",value.name);
                }
            }
        }
        if(added2 != null){
            nodeMap.put(added2.name,added2);
        }
    }
}