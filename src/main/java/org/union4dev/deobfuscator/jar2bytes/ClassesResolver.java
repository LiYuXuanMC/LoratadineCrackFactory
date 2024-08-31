package org.union4dev.deobfuscator.jar2bytes;

import org.union4dev.deobfuscator.asm.SuperClassWriter;
import org.union4dev.deobfuscator.jar2bytes.entity.Tuple;
import org.union4dev.deobfuscator.jar2bytes.util.ZipUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ClassesResolver {
	public static ClassesResolver INSTANCE;
	private File inputFile;
	private final File outputFile;

	private final List<String> filterClasses = new ArrayList<>();

	private final List<Tuple<ClassNode, ClassReader>> classNodes = new ArrayList<>();

	private final Map<ClassNode, byte[]> classNodeOpcodesMap = new HashMap<>();

	private int writerFlag = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
	ClassesResolver(){
		INSTANCE = this;
		this.outputFile = new File("temp\\_output.jar");
	}
	public ClassesResolver addFilterClass(String className) {
		filterClasses.add(className);
		return this;
	}

	public ClassesResolver setInputFile(File inputFile) {
		this.inputFile = inputFile;
		return this;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public ClassesResolver resolve() throws IOException {

		{boolean ignore = outputFile.createNewFile();}

		ZipOutputStream outputZipFile = new ZipOutputStream(Files.newOutputStream(outputFile.toPath()));
		try (ZipFile jarFile = new ZipFile(inputFile)) {
			Enumeration<? extends ZipEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				try (InputStream inputStream = jarFile.getInputStream(entry)) {
					if (!entry.getName().endsWith(".class")) {
						continue;
					} else if (filterClasses.stream().anyMatch(entry.getName()::startsWith)) {
						ZipUtil.PutFile(outputZipFile, entry, inputStream);
						continue;
					}
					//System.out.println("Processing: " + entry.getName());
					byte[] opcodes = ZipUtil.readStream(inputStream);
					ClassReader classReader = new ClassReader(opcodes);
					ClassNode classNode = new ClassNode();
					classNodeOpcodesMap.put(classNode, opcodes);
					classReader.accept(classNode, 0);
					classNodes.add(new Tuple<>(classNode, classReader));
				}
			}
			for (Tuple<ClassNode, ClassReader> tuple : classNodes) {
				writerFlag = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
				ClassWriter classWriter = new SuperClassWriter(writerFlag);
				ClassNode classNode = tuple.getFirst();
				classNode.accept(classWriter);
				byte[] writeContent = classWriter.toByteArray();
				ZipUtil.PutFile(outputZipFile, new ZipEntry(classNode.name + ".class"), writeContent);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		outputZipFile.close();
		return this;
	}
}
