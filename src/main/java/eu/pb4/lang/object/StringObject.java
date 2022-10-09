package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

public class StringObject extends XObject<String> {
    private final String value;

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
