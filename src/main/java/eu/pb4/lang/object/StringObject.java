package eu.pb4.lang.object;

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
    public XObject<?> multiply(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            var builder = new StringBuilder();

            for (int i = 0; i < numberObject.value(); i++) {
                builder.append(this.value);
            }

            return new StringObject(builder.toString());

        }

        return super.multiply(object);
    }
}
