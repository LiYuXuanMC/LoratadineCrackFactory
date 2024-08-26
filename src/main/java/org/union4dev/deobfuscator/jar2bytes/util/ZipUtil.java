package org.union4dev.deobfuscator.jar2bytes.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static byte[] readStream(InputStream inputStream) throws IOException {
        return IOUtils.toByteArray(inputStream);
    }

    public static void PutFile(ZipOutputStream outputStream, ZipEntry name, InputStream source) throws IOException {
        PutFile(outputStream, name, readStream(source));
    }

    public static void PutFile(ZipOutputStream outputStream, ZipEntry name, byte[] source) throws IOException {
        outputStream.putNextEntry(new ZipEntry(name.getName()));
        outputStream.write(source);
        outputStream.closeEntry();
    }

}
