package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.util.GenUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.PrimitiveIterator;

public class StringObject extends XObject<String> {
    private final String value;
    private XObject<?> subStringFunc;

    public StringObject(String value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return this.value;
    }

    @Override
    public byte[] asBytes(Expression.Position info) throws InvalidOperationException {
        return this.value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public @Nullable String asJava() {
        return this.value;
    }

    @Override
    public String type() {
        return "string";
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "length" -> NumberObject.of(this.value.length());
            case "buffer" -> new ByteArrayObject(this.value.getBytes(StandardCharsets.UTF_8));
            case "substring" -> this.getSubString();
            default -> super.get(scope, key, info);
        };
    }

    private XObject<?> getSubString() {
        if (this.subStringFunc != null) {
            this.subStringFunc = new JavaFunctionObject((s, a, i) -> {
                GenUtils.argumentCount(a, 1, i);

                if (a.length == 2) {
                    return new StringObject(StringObject.this.value.substring(a[0].asInt(i), a[1].asInt(i)));
                } else {
                    return new StringObject(StringObject.this.value.substring(a[0].asInt(i)));
                }
            });
        }
        return this.subStringFunc;
    }

    @Override
    public XObject<?> multiply(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            var builder = new StringBuilder();

            for (int i = 0; i < numberObject.value(); i++) {
                builder.append(this.value);
            }

            return new StringObject(builder.toString());

        }

        return super.multiply(scope, object, info);
    }

    public static XObject<?> create(ObjectScope scope, XObject<?>[] args, Expression.Position position) throws InvalidOperationException {
        if (args.length == 0) {
            return new StringObject("");
        } else {
            if (args[0] instanceof ByteArrayObject bufferObject) {
                return new StringObject(new String(bufferObject.asBytes(position), StandardCharsets.UTF_8));
            } else {
                return new StringObject(args[0].asString());
            }
        }
    }

    @Override
    public Iterator<XObject<?>> iterator(ObjectScope scope, Expression.Position info) throws InvalidOperationException {
        return new NumberIterator(this.value.codePoints().iterator());
    }

    private record NumberIterator(PrimitiveIterator.OfInt iterator) implements Iterator<XObject<?>> {
        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public XObject<?> next() {
            return NumberObject.of(this.iterator.next());
        }
    }

    @Override
    public boolean isContextless() {
        return true;
    }
}
