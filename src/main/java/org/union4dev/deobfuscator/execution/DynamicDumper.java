package org.union4dev.deobfuscator.execution;

import org.tinylog.Logger;
import org.union4dev.deobfuscator.RunnableMain;
import org.union4dev.deobfuscator.util.ClassNodeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.zip.ZipOutputStream;

public class DynamicDumper {
    public static void init() {
        System.load(new File("lib_monitor.dll").getAbsolutePath());
    }

    public static String ParseClassName(byte[] bytes){
        String name = ClassNodeUtil.quickParseBytes(bytes).name;
        Logger.info("Defined Class {}",name);
        return name;
    }

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
