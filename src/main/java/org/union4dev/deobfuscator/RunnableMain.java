package org.union4dev.deobfuscator;

import org.union4dev.deobfuscator.configuration.Configuration;
import org.union4dev.deobfuscator.transformer.implement.TestTransformer;

public class RunnableMain {

    public static void main(String[] args) {
        final Configuration configuration = new Configuration();
        configuration.setInput("D:\\deobfuscator\\Test.jar");
        configuration.setOutput("D:\\deobfuscator\\Test-deobf.jar");
        configuration.addClasspath("C:\\Program Files\\Java\\jre1.8.0_361\\lib\\rt.jar");
        configuration.addTransformer(new TestTransformer());
        Deobfuscator.INSTANCE.run(configuration);
    }
}
