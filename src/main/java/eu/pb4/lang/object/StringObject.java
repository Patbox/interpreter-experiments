package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.util.GenUtils;
import org.jetbrains.annotations.Nullable;

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
            case "length" -> new NumberObject(this.value.length());
            case "substring" -> this.getSubString();
            default -> super.get(scope, key, info);
        };
    }

    private XObject<?> getSubString() {
        if (this.subStringFunc != null) {
            this.subStringFunc = new JavaFunctionObject((s, a, i) -> {
                GenUtils.argumentCount(a, 1, i);

                if (a.length == 2) {
                    return new StringObject(StringObject.this.value.substring(a[0].asInt(), a[1].asInt()));
                } else {
                    return new StringObject(StringObject.this.value.substring(a[0].asInt()));
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
}
