package eu.pb4.lang.test;

import eu.pb4.lang.Runtime;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String... main) throws Exception {
        var input = Files.readString(Path.of(Main.class.getClassLoader().getResource("test.pbs").toURI()));


        /*for (var token : new Tokenizer(new StringReader(input)).getTokens()) {
            System.out.println(token);
        }*/

        var runtime = new Runtime();
        runtime.defaultGlobals();

        runtime.registerImporter((x) -> {
            if (x.startsWith("testjar:")) {
                try {
                    return runtime.importAndRun(x, Files.readString(Path.of(Main.class.getClassLoader().getResource( x.substring("testjar:".length() )+ ".pbs").toURI()))).scope().getExportObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        });

        runtime.getScope().freeze();

        var time = System.currentTimeMillis();
        runtime.run(input);
        runtime.importAndRun("testjar:invalid");
        System.out.println("Time: " + (System.currentTimeMillis() - time));

    }
}
