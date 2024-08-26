package org.union4dev.deobfuscator.jar2bytes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Jar2Bytes {
	public static void start() throws IOException {
		Path path = Paths.get("temp");
		Files.createDirectories(path);

		File tempOutputFile = new ClassesResolver()
				.setInputFile(new File("output.jar"))
				.resolve()
				.getOutputFile();

		HeaderConverter.processInputJar(tempOutputFile.getPath(),"classes.hpp");
		{boolean ignore = tempOutputFile.delete();}
	}
}
