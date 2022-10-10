package eu.pb4.lang.test;

import eu.pb4.lang.Runtime;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.util.ObjectBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
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
                    return runtime.importAndRun(x, Files.readString(Path.of(Main.class.getClassLoader().getResource(x.substring("testjar:".length()) + ".pbs").toURI()))).scope().getExportObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        });

        var frame = new Frame(){
            public BufferedImage image;

            public void update(Graphics g){
                paint(g);
            }

            @Override
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, new ImageObserver() {
                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        return false;
                    }
                });
            }
        };

        frame.image = new BufferedImage(128 * 4, 128 * 3, BufferedImage.TYPE_INT_RGB);
        frame.setSize(128 * 4, 128 * 3);
        frame.setVisible(true);

        runtime.getScope().declareVariable("Display", new ObjectBuilder()
                .varArg("setPixel", (scope, args, info) -> {
                    frame.image.setRGB(args[0].asInt(), args[1].asInt(), args[2].asInt());
                    return XObject.NULL;
                })
                .varArg("clear", (scope, args, info) -> {
                    var c = args[0].asInt();
                    for (int x = 0; x < frame.image.getWidth(); x++) {
                        for (int y = 0; y < frame.image.getHeight(); y++) {
                            frame.image.setRGB(x, y, c);
                        }
                    }
                    return XObject.NULL;
                })
                .noArg("update", () -> {
                    BufferedImage b = new BufferedImage(frame.image.getWidth(), frame.image.getHeight(), frame.image.getType());
                    Graphics g = b.getGraphics();
                    g.drawImage(frame.image, 0, 0, null);
                    g.dispose();
                    frame.repaint();
                    frame.image = b;
                })

                .build());

        runtime.getScope().freeze();

        var time = System.currentTimeMillis();
        runtime.run(input);
        runtime.importAndRun("testjar:invalid");
        System.out.println("Time: " + (System.currentTimeMillis() - time));

        while (true) {
            runtime.tick();

            Thread.sleep(1);
        }


    }
}
