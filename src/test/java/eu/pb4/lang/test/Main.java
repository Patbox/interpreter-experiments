package eu.pb4.lang.test;

import eu.pb4.lang.Runtime;
import eu.pb4.lang.parser.StringReader;
import eu.pb4.lang.parser.Tokenizer;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String... main) throws Exception {
        var input = Files.readString(Path.of(Main.class.getClassLoader().getResource("test.pbs").toURI()));


        for (var token : new Tokenizer(new StringReader(input)).getTokens()) {
            System.out.println(token);
        }

        var runtime = new Runtime();
        runtime.defaultGlobals();
        runtime.getScope().freeze();
        System.out.println(runtime.run(input));
    }
}
