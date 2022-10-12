package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.util.GenUtils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class ByteArrayObject extends XObject<byte[]> {
    private byte[] bytes;

    private final XObject<?> functionResize = JavaFunctionObject.ofVoid((s, arg, c) -> {
        GenUtils.argumentCount(arg, 1, c);
        this.bytes = Arrays.copyOf(this.bytes, arg[0].asInt(c));
    });

    private final XObject<?> functionFill = JavaFunctionObject.ofVoid((s, arg, c) -> {
        GenUtils.argumentCount(arg, 1, c);
        Arrays.fill(this.bytes, (byte) arg[0].asInt(c));
    });

    public ByteArrayObject(int size) {
        this.bytes = new byte[size];
    }

    public ByteArrayObject(byte[] bytes) {
        this.bytes = bytes;
    }

    public static XObject<?> create(ObjectScope scope, XObject<?>[] xObjects, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(xObjects, 1, info);

        return new ByteArrayObject(xObjects[0].asInt(info));
    }

    @Override
    public void set(ObjectScope scope, XObject<?> key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        var val = key.asInt(info);

        if (val >= this.bytes.length) {
            throw new InvalidOperationException(info, "tried to access index " + val + " out of " + this.bytes);
        } else if (val < 0) {
            throw new InvalidOperationException(info, "tried to access index below zero -> " + val);
        }

        bytes[val] = (byte) object.asDouble(info);
    }

    @Override
    public XObject<?> get(ObjectScope scope, XObject<?> key, Expression.Position info) throws InvalidOperationException {
        var val = key.asInt(info);

        if (val >= this.bytes.length) {
            throw new InvalidOperationException(info, "tried to access index " + val + " out of " + this.bytes);
        } else if (val < 0) {
            throw new InvalidOperationException(info, "tried to access index below zero -> " + val);
        }

        return NumberObject.of(bytes[val]);
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "length" -> NumberObject.of(this.bytes.length);
            case "resize" -> this.functionResize;
            case "fill" -> this.functionFill;
            case "reader" -> new StreamReaderObject(new ByteArrayInputStream(this.bytes));
            default -> super.get(scope, key, info);
        };
    }

    @Override
    public void set(ObjectScope scope, String key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        switch (key) {
            case "length" -> this.bytes = Arrays.copyOf(this.bytes, object.asInt(info));
            default -> super.set(scope, key, object, info);
        }
    }

    @Override
    public @Nullable byte[] asJava() {
        return this.bytes;
    }

    @Override
    public byte[] asBytes(Expression.Position info) throws InvalidOperationException {
        return this.bytes;
    }

    @Override
    public String asString() {
        return "<buffer>";
    }

    @Override
    public String type() {
        return "buffer";
    }
}
