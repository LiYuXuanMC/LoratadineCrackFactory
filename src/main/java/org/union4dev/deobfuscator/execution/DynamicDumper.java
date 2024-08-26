package org.union4dev.deobfuscator.execution;

import org.tinylog.Logger;
import org.union4dev.deobfuscator.util.ClassNodeUtil;

import java.io.File;

public class DynamicDumper {
    public static void init() {
        System.load(new File("lib_monitor.dll").getAbsolutePath());
    }

    public static String ParseClassName(byte[] bytes){
        String name = ClassNodeUtil.quickParseBytes(bytes).name;
        Logger.info("Defined Class {}",name);
        return name;
    }
}
