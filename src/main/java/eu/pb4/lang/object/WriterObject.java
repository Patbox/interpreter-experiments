package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutput;


public class WriterObject extends XObject<DataOutput> {
    private final DataOutput output;

    private final JavaFunctionObject writeByte = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.write(b[0].asInt(c));
    });

    private final JavaFunctionObject writeBuffer = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.write(b[0].asBytes(c));
    });

    private final JavaFunctionObject writeShort = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.writeShort(b[0].asInt(c));
    });

    private final JavaFunctionObject writeInt = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.writeInt(b[0].asInt(c));
    });

    private final JavaFunctionObject writeLong = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.writeLong((long) b[0].asDouble(c));
    });

    private final JavaFunctionObject writeString = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.writeUTF(b[0].asString());
    });

    private final JavaFunctionObject writeDouble = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.writeDouble(b[0].asDouble(c));
    });

    private final JavaFunctionObject writeFloat = JavaFunctionObject.ofVoid((a, b, c) -> {
        WriterObject.this.output.writeFloat((float) b[0].asDouble(c));
    });

    public WriterObject(DataOutput output) {
        this.output = output;
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "write" -> this.writeByte;
            case "writeBuffer" -> this.writeBuffer;
            case "writeShort" -> this.writeShort;
            case "writeInt" -> this.writeInt;
            case "writeLong" -> this.writeLong;
            case "writeFloat" -> this.writeFloat;
            case "writeDouble" -> this.writeDouble;
            case "writeString" -> this.writeString;
            default -> super.get(scope, key, info);
        };
    }

    @Override
    public @Nullable DataOutput asJava() {
        return this.output;
    }

    @Override
    public String asString() {
        return "<writer>";
    }

    @Override
    public String type() {
        return "writer";
    }
}
