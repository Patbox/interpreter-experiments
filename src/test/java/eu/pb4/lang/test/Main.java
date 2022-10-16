package eu.pb4.lang.test;

import eu.pb4.lang.runtime.Runtime;
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

        frame.image = new BufferedImage(128 * 5, 128 * 4, BufferedImage.TYPE_INT_RGB);
        frame.setSize(frame.image.getWidth(), frame.image.getHeight());
        frame.setVisible(true);

        runtime.setGlobal("Display", new ObjectBuilder()
                .varArg("setPixel", (scope, args, info) -> {
                    var x = args[0].asInt(info);
                    var y = args[1].asInt(info);

                    if (x < 0 || y < 0 || x >= frame.image.getWidth() || y >= frame.image.getHeight()) {
                        return XObject.NULL;
                    }

                    frame.image.setRGB(x, y, args[2].asInt(info));
                    return XObject.NULL;
                })
                .varArg("clear", (scope, args, info) -> {
                    var c = args[0].asInt(info);
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

        var time = System.currentTimeMillis();
        var bytecode = Runtime.buildByteCode(input);
        System.out.println("ByteCode: " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        runtime.execute(bytecode);
        //runtime.importAndRun("testjar:class");
        System.out.println("Time: " + (System.currentTimeMillis() - time));

        while (true) {
            runtime.tick();
            Thread.sleep(1);
        }
    }
}
