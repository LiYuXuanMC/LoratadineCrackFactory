package org.union4dev.deobfuscator.configuration;

import com.beust.jcommander.Parameter;

public class ProcessConfiguration {
    @Parameter(names = "-input", description = "Input DLL file path", required = true)
    public String inputFilePath;

    @Parameter(names = "-output", description = "Output DLL file path", required = true)
    public String outputFilePath;

    @Parameter(names = "-libPath", description = "libraries file path", required = true)
    public String libsPath;

    @Parameter(names = "-msBuildPath", description = "MSBuild executable path", required = true)
    public String msBuildPath;

    @Parameter(names = "-fakeThread", description = "Use a fake Render thread for dump some shit")
    public boolean useLwjgl;

    @Parameter(names = {"-help", "-h"}, help = true, description = "Display help")
    public boolean help;
}
