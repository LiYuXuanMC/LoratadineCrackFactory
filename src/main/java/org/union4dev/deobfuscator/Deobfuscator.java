package org.union4dev.deobfuscator;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.asm.HierarchyClass;
import org.union4dev.deobfuscator.configuration.Configuration;
import org.union4dev.deobfuscator.transformer.Transformer;
import org.union4dev.deobfuscator.util.ClassNodeUtil;
import org.union4dev.deobfuscator.util.SecurityChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Deobfuscator {

    public static final Deobfuscator INSTANCE = new Deobfuscator();

    private SecurityManager securityManager;
    private SecurityChecker securityChecker;
    private final Map<String, ClassNode> classNodeMap = new HashMap<>();
    private final Map<String, ClassNode> classpathMap = new HashMap<>();
    private final Map<String, HierarchyClass> hierarchy = new HashMap<>();

    public void run(Configuration configuration) {
        securityManager = System.getSecurityManager();
        securityChecker = configuration.getSecurityChecker();
        if (configuration.getInput() == null || configuration.getOutput() == null) {
            Logger.warn("Please select the input file and the output file.");
            return;
        }
        if (configuration.getTransformers().isEmpty()) {
            Logger.warn("Please select at least one transformer.");
            return;
        }

        // loading.
        loadTarget(configuration);
        loadClasspath(configuration);

        // apply transformers.
        applyTransformers(configuration);

        // writing.
        writeTarget(configuration);
    }

    public void loadSecurityChecker() {
        if (securityChecker != null)
            System.setSecurityManager(securityChecker);
    }

    public void resetSecurityChecker() {
        if (securityChecker != null)
            System.setSecurityManager(securityManager);
    }

    private void applyTransformers(Configuration configuration) {
        for (Transformer transformer : configuration.getTransformers()) {
            transformer.transform(classNodeMap);
        }
    }

    private void loadTarget(Configuration configuration) {
        Logger.info("Loading target: " + configuration.getInput());
        try (JarFile jarFile = new JarFile(configuration.getInput())) {
            final Enumeration<? extends JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry ent = entries.nextElement();
                if (ent.getName().endsWith(".class")) {
                    final ClassReader classReader = new ClassReader(jarFile.getInputStream(ent));
                    final ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    classNodeMap.put(ent.getName().substring(0, ent.getName().length() - 6), classNode);
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void loadClasspath(Configuration configuration) {
        classpathMap.putAll(classNodeMap);
        for (String library : configuration.getClasspath()) {
            Logger.info("Loading classpath: " + library);
            try (JarFile jarFile = new JarFile(library)) {
                final Enumeration<? extends JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry ent = entries.nextElement();
                    if (ent.getName().endsWith(".class")) {
                        final ClassReader classReader = new ClassReader(jarFile.getInputStream(ent));
                        final ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                        classpathMap.put(ent.getName().substring(0, ent.getName().length() - 6), classNode);
                    }
                }
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }

    private void writeTarget(Configuration configuration) {
        Logger.info("Writing target: " + configuration.getOutput());
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(Paths.get(configuration.getOutput())))) {
            // Writing resources.
            try (JarFile jarFile = new JarFile(configuration.getInput())) {
                final Enumeration<? extends JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry ent = entries.nextElement();
                    if (!ent.getName().endsWith(".class")) {
                        final JarEntry file = new JarEntry(ent.getName());
                        jarOutputStream.putNextEntry(file);
                        jarOutputStream.write(IOUtils.toByteArray(jarFile.getInputStream(ent)));
                        jarOutputStream.closeEntry();
                    }
                }
            }

            // Writing classes.
            for (ClassNode classNode : classNodeMap.values()) {
                final byte[] bytes = ClassNodeUtil.parseNode(classNode);
                if (bytes != null) {
                    jarOutputStream.putNextEntry(new JarEntry(classNode.name + ".class"));
                    jarOutputStream.write(bytes);
                    jarOutputStream.closeEntry();
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void buildHierarchy(ClassNode classNode) {
        final List<String> processed = new ArrayList<>();
        final Queue<ClassNode> processQueue = new ArrayDeque<>();
        processQueue.add(classNode);
        while (!processQueue.isEmpty()) {
            final List<ClassNode> joinQueue = new ArrayList<>();
            final ClassNode poll = processQueue.poll();
            if (poll.name.equals("java/lang/Object")) {
                continue;
            }

            final HierarchyClass current = createHierarchy(poll);
            final ClassNode superClass = getNodeFromClasspath(poll.superName);
            if (superClass == null) {
                throw new RuntimeException("Failed to load: " + poll.name);
            }
            final HierarchyClass superHierarchy = createHierarchy(superClass);
            superHierarchy.child.add(poll);
            current.parents.add(superClass);
            joinQueue.add(superClass);

            for (String interfaceName : poll.interfaces) {
                final ClassNode interfaceNode = getNodeFromClasspath(interfaceName);
                if (interfaceNode == null) {
                    throw new RuntimeException("Failed to load: " + interfaceName);
                }
                final HierarchyClass interfaceHierarchy = createHierarchy(interfaceNode);
                interfaceHierarchy.child.add(poll);
                current.parents.add(interfaceNode);
                joinQueue.add(interfaceNode);
            }

            for (ClassNode node : joinQueue) {
                if (!processed.contains(node.name)) {
                    processed.add(node.name);
                    processQueue.add(node);
                }
            }
        }
    }

    private HierarchyClass createHierarchy(ClassNode classNode) {
        if (hierarchy.containsKey(classNode.name)) {
            return hierarchy.get(classNode.name);
        } else {
            final HierarchyClass hierarchyClass = new HierarchyClass(classNode);
            hierarchy.put(classNode.name, hierarchyClass);
            return hierarchyClass;
        }
    }

    public HierarchyClass getHierarchyClass(String classNode) {
        final HierarchyClass tree = hierarchy.get(classNode);
        if (tree == null) {
            buildHierarchy(getNodeFromClasspath(classNode));
            return getHierarchyClass(classNode);
        }
        return tree;
    }

    public ClassNode getNodeFromClasspath(String reference) {
        final ClassNode clazz = classpathMap.get(reference);
        if (clazz == null) {
            throw new RuntimeException("Missing reference: " + reference);
        }
        return clazz;
    }

    public Map<String, ClassNode> getClasspathMap() {
        return classpathMap;
    }
}
