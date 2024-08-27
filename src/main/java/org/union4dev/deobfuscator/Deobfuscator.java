package org.union4dev.deobfuscator;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.asm.HierarchyClass;
import org.union4dev.deobfuscator.configuration.Configuration;
import org.union4dev.deobfuscator.transformer.Transformer;
import org.union4dev.deobfuscator.transformer.implement.CrackTransformer_V1_2;
import org.union4dev.deobfuscator.util.ClassNodeUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

public class Deobfuscator {

    public static final Deobfuscator INSTANCE = new Deobfuscator();
    private final Map<String, ClassNode> classNodeMap = new HashMap<>();
    private final Map<String, ClassNode> classpathMap = new HashMap<>();
    private final Map<String, HierarchyClass> hierarchy = new HashMap<>();
    private final List<String> notModified = new ArrayList<>();
    private FileSystem jrtFileSystem;

    private ArrayList<Path> modulePaths;

    public void run(Configuration configuration) {
        if (configuration.getInput() == null || configuration.getOutput() == null) {
            Logger.warn("Please select the input file and the output file.");
            return;
        }
        if (configuration.getTransformers().isEmpty()) {
            Logger.warn("Please select at least one transformer.");
            return;
        }

        final File javaHome = new File(System.getProperty("java.home"));

        if (Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) > 8) {
            initJRTFileSystem(javaHome);
        }else {
            Logger.error("This software require java 17.");
            System.exit(0);
        }


        // loading.
        loadTarget(configuration);
        loadClasspath(configuration);

        notModified.addAll(classNodeMap.keySet());

        // apply transformers.
        applyTransformers(configuration);

        // writing.
        writeTarget(configuration);
    }

    public List<String> getNotModified() {
        return notModified;
    }

    public void modified(String name){
        notModified.remove(name);
    }

    public void initJRTFileSystem(File javaHome) {
        try {
            Logger.info("Initializing JRT File system...");
            jrtFileSystem = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.singletonMap("java.home", javaHome.getAbsolutePath()));
            modulePaths = new ArrayList<>();

            final Stream<Path> moduleStream = Files.list(jrtFileSystem.getPath("modules"));

            moduleStream.forEach((m) -> modulePaths.add(m));
            moduleStream.close();
        } catch (IOException e) {
            Logger.error("Unable to init JRTFileSystem.", e);
            e.printStackTrace();
        }
    }


    public void loadSecurityChecker() {
    }

    public void resetSecurityChecker() {
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
                    if (!ent.getName().endsWith(".class") || notModified.contains(ent.getName().replace(".class",""))) {
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
                if (bytes != null && !notModified.contains(classNode.name)) {
                    Logger.info("Write class {}",classNode.name);
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
