package org.union4dev.deobfuscator.configuration;

import org.union4dev.deobfuscator.transformer.Transformer;
import org.union4dev.deobfuscator.util.SecurityChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration {

    private String input, output;
    private SecurityChecker securityChecker;
    private final List<String> classpath = new ArrayList<>();
    private final List<Transformer> transformers = new ArrayList<>();

    public void setSecurityChecker(SecurityChecker securityChecker) {
        this.securityChecker = securityChecker;
    }

    public SecurityChecker getSecurityChecker() {
        return securityChecker;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<String> getClasspath() {
        return classpath;
    }

    public void addClasspath(String path) {
        classpath.add(path);
    }

    public void addTransformer(Transformer... transformer) {
        transformers.addAll(Arrays.asList(transformer));
    }

    public List<Transformer> getTransformers() {
        return transformers;
    }
}
