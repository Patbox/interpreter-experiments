package eu.pb4.lang.runtime;

import eu.pb4.lang.object.NumberObject;
import eu.pb4.lang.object.StringObject;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.util.GenUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class StaticObjectConsumer {
    private int id = 0;
    private final ByteArrayOutputStream bb = new ByteArrayOutputStream();
    private final DataOutputStream stream = new DataOutputStream(this.bb);

    public int putDouble(double value) throws IOException {
        var id = this.id++;
        if (value == (int) value) {
            this.stream.write(1);
            this.stream.writeInt((int) value);
        } else {
            this.stream.write(0);
            this.stream.writeDouble(value);
        }

        return id;
    }

    public int putStringUTF(String value) throws IOException {
        var id = this.id++;
        this.stream.write(2);
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        this.stream.writeInt(bytes.length);
        this.stream.write(bytes);
        return id;
    }

    public int putStringKey(String value) throws IOException {
        var id = this.id++;
        this.stream.write(3);
        GenUtils.writeIdentifierString(this.stream, value);
        return id;
    }

    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.id);
        stream.write(bb.toByteArray());
    }

    public static XObject<?>[] decode(ByteArrayReader reader) throws IOException {
        var count = reader.readInt();
        var arr = new XObject<?>[count];

        for (int i = 0; i < count; i++) {
            var id = reader.read();

            arr[i] = switch (id) {
                case 0 -> new NumberObject(reader.readDouble());
                case 1 -> new NumberObject(reader.readInt());
                case 2 -> {
                    var stringArr = new byte[reader.readInt()];
                    reader.read(stringArr);
                    yield new StringObject(new String(stringArr, StandardCharsets.UTF_8));
                }
                case 3 -> new StringObject(GenUtils.readIdentifierString(reader));
                default -> XObject.NULL;
            };
        }


        return arr;
    }
}
