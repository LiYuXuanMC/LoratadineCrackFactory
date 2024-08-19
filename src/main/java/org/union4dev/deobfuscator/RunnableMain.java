package org.union4dev.deobfuscator;

import org.union4dev.deobfuscator.configuration.Configuration;
import org.union4dev.deobfuscator.transformer.implement.AllatoriStringTransformer;
import org.union4dev.deobfuscator.util.SecurityChecker;

public class RunnableMain {

    public static void main(String[] args) {
        final Configuration configuration = new Configuration();
        configuration.setSecurityChecker(new SecurityChecker(true, true, true, true, true, true, false, true, true, true, true, true, true));
        configuration.setInput("D:\\deobfuscator\\obf-mousegestures-test.jar");
        configuration.setOutput("D:\\deobfuscator\\obf-mousegestures-test-deobf.jar");
        configuration.addClasspath("D:\\deobfuscator\\obf-mousegestures-1.2.jar");
        configuration.addClasspath("C:\\Program Files\\Java\\jre1.8.0_361\\lib\\rt.jar");
        configuration.addTransformer(new AllatoriStringTransformer());
        Deobfuscator.INSTANCE.run(configuration);
    }
}
