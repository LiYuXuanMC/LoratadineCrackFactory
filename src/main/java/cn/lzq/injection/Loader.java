package cn.lzq.injection;

import org.tinylog.Logger;
import org.union4dev.deobfuscator.RunnableMain;
import org.union4dev.deobfuscator.execution.LWJGLDummy;
import org.union4dev.deobfuscator.util.ClassNodeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

public class Loader {
    public static int a(byte[][] classData) {
        try {
            File dump = new File("dumped.jar");
            if(dump.exists()){
                Files.delete(dump.toPath());
            }

            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("dumped.jar"));

            for(byte[] classBytes : classData) {
                String name = ClassNodeUtil.quickParseBytes(classBytes).name;
                Logger.info("Dumped Class {}.",name );
                if(!name.startsWith("cn/lzq") || name.endsWith("InjectionEndpoint") || name.endsWith("Fucker")){
                    zipOutputStream.putNextEntry(new JarEntry(name + ".class"));
                    zipOutputStream.write(classBytes);
                    zipOutputStream.closeEntry();
                }
            }

            zipOutputStream.finish();
            zipOutputStream.close();
            Logger.info("Finished dump, start transform");
            RunnableMain.transform();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static byte[][] a(int size) {
        return new byte[size][];
    }
}
