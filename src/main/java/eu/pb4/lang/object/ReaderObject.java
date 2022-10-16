package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;


public class ReaderObject extends XObject<DataInput> {
    private final DataInput input;

    private final JavaFunctionObject readByte = new JavaFunctionObject((a, b, c) -> NumberObject.of(ReaderObject.this.input.readUnsignedByte()));

    private final JavaFunctionObject readBuffer = new JavaFunctionObject((a, b, c) -> {
        var obj = new ByteArrayObject(b[0].asInt(c));
        ReaderObject.this.input.readFully(obj.asJava());
        return obj;
    });

    private final JavaFunctionObject readShort = new JavaFunctionObject((a, b, c) -> NumberObject.of(ReaderObject.this.input.readUnsignedShort()));

    private final JavaFunctionObject readInt = new JavaFunctionObject((a, b, c) -> NumberObject.of(ReaderObject.this.input.readInt()));

    private final JavaFunctionObject readLong = new JavaFunctionObject((a, b, c) -> NumberObject.of(ReaderObject.this.input.readLong()));

    private final JavaFunctionObject readString = new JavaFunctionObject((a, b, c) -> new StringObject(ReaderObject.this.input.readUTF()));

    private final JavaFunctionObject readDouble = new JavaFunctionObject((a, b, c) -> NumberObject.of(ReaderObject.this.input.readDouble()));

    private final JavaFunctionObject readFloat = new JavaFunctionObject((a, b, c) -> NumberObject.of(ReaderObject.this.input.readFloat()));

    public ReaderObject(DataInput input) {
        this.input = input;
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "read", "readByte" -> this.readByte;
            case "readBuffer" -> this.readBuffer;
            case "readShort" -> this.readShort;
            case "readInt" -> this.readInt;
            case "readLong" -> this.readLong;
            case "readFloat" -> this.readFloat;
            case "readDouble" -> this.readDouble;
            case "readString" -> this.readString;
            default -> super.get(scope, key, info);
        };
    }

    @Override
    public @Nullable DataInput asJava() {
        return this.input;
    }

    @Override
    public String asString() {
        return "<reader>";
    }

    @Override
    public String type() {
        return "reader";
    }
}
