package org.union4dev.deobfuscator.configuration;

import com.beust.jcommander.Parameter;

public class ProcessConfiguration {
    @Parameter(names = "-input", description = "Input DLL file path", required = true)
    public String inputFilePath;

    @Parameter(names = "-output", description = "Output DLL file path", required = true)
    public String outputFilePath;

    @Parameter(names = "-rtPath", description = "rt.jar file path", required = true)
    public String rtPath;

    @Parameter(names = "-binPath", description = "forge client jar file path", required = true)
    public String binPath;

    @Parameter(names = "-msBuildPath", description = "MSBuild executable path", required = true)
    public String msBuildPath;

    @Parameter(names = {"-help", "-h"}, help = true, description = "Display help")
    public boolean help;
}
