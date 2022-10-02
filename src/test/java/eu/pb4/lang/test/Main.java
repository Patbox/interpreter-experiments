package eu.pb4.lang.test;

import eu.pb4.lang.Runtime;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String... main) throws Exception {
        var input = Files.readString(Path.of(Main.class.getClassLoader().getResource("test.pbs").toURI()));

        var runtime = new Runtime();
        runtime.defaultGlobals();
        runtime.getScope().freeze();
        runtime.run(input);
    }
}
