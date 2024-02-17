package org.union4dev.deobfuscator.transformer.implement;

import org.objectweb.asm.tree.ClassNode;
import org.union4dev.deobfuscator.transformer.Transformer;

import java.util.Map;

public class TestTransformer extends Transformer {
    @Override
    public void transform(Map<String, ClassNode> nodeMap) {
        for (ClassNode classNode : nodeMap.values()) {
            System.out.println(classNode.name);
        }
    }
}
