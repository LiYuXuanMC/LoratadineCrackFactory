package org.union4dev.deobfuscator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.union4dev.deobfuscator.configuration.ProcessConfiguration;
import org.union4dev.deobfuscator.jar2bytes.Jar2Bytes;
import org.tinylog.Logger;
import org.union4dev.deobfuscator.configuration.Configuration;
import org.union4dev.deobfuscator.execution.DynamicDumper;
import org.union4dev.deobfuscator.execution.LWJGLDummy;
import org.union4dev.deobfuscator.transformer.implement.CrackTransformer;

import javax.annotation.processing.Processor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RunnableMain {

    //private static String msBuildPath = "C:\\Program Files\\Microsoft Visual Studio\\2022\\Community\\MSBuild\\Current\\Bin\\MSBuild.exe";

    public static ProcessConfiguration processConfiguration;

    public static void main(String[] args) throws InterruptedException {
        Logger.info("Loratadine Crack Factory V1");

        processConfiguration = new ProcessConfiguration();

        JCommander commander = JCommander.newBuilder()
                .addObject(processConfiguration)
                .programName("LoratadineCrackFactory.exe")
                .build();

        try {
            commander.parse(args);
        }catch (ParameterException e){
            commander.usage();
            return;
        }

        if (processConfiguration.help) {
            commander.usage();
            return;
        }

        DynamicDumper.init();
        LWJGLDummy.start();
    }

    public static void transform() throws IOException {
        final Configuration configuration = new Configuration();
        configuration.setInput("dumped.jar");
        configuration.setOutput("output.jar");
        configuration.addClasspath(processConfiguration.rtPath);
        configuration.addClasspath(processConfiguration.binPath);
        configuration.addTransformer(new CrackTransformer());
        Deobfuscator.INSTANCE.run(configuration);
        Logger.info("Transformed, start generating cracked dll...");
        File classesCpp = new File("loader/loader/src/base/classes/classes.hpp");
        if(classesCpp.exists()){
            Logger.info("Found classes.cpp in {}, deleting",classesCpp.getAbsolutePath());
            Files.delete(classesCpp.toPath());
        }

        try {
            Jar2Bytes.start();

            File dllPath = new File("loader/x64/Release/loader.dll");
            File classesNew = new File("classes.hpp");
            if(classesNew.exists()){
                Logger.info("Successfully generated header, moving");
                Files.copy(classesNew.toPath(),classesCpp.toPath());
                Logger.info("Ready for generate cracked dll");

                if(dllPath.exists())
                    Files.delete(dllPath.toPath());

                List<String> command = new ArrayList<>();
                command.add(processConfiguration.msBuildPath);
                command.add("/m");
                command.add("/p:Configuration=Release");
                command.add("/property:PreferredUILang=en-US");
                command.add(".");

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(new File("loader"));

                Process process = processBuilder.start();
                try (InputStream inputStream = process.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Logger.info(line);
                    }
                } finally {
                    process.destroy();
                    if(dllPath.exists()){
                        File outPath = new File(processConfiguration.outputFilePath);
                        if(outPath.exists())
                        {
                            Files.delete(outPath.toPath());
                        }
                        Files.copy(dllPath.toPath(),outPath.toPath());
                        Logger.info("Successfully cracked, your dll in " + outPath.getAbsolutePath());
                        while (true){}
                    }else {
                        Logger.error("Failed to build dll, please check your msbuild config.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
