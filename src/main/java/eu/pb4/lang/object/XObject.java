package eu.pb4.lang.object;

import eu.pb4.lang.expression.DirectObjectExpression;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class XObject<T> {

    public static final XObject<?> NULL = new XObject<>() {
        @Override
        public String asString() {
            return "<null>";
        }

        @Override
        public Object asJava() {
            return null;
        }
    };
    private Expression expression = new DirectObjectExpression(this);
    private StringObject toStringValue;

    @Nullable
    public abstract T asJava();

    public Expression asExpression() {
        return this.expression;
    }


    public XObject<?> divide(XObject<?> object) {
        throw new UnsupportedOperationException("This object doesn't support division");
    }

    public XObject<?> multiply(XObject<?> object) {
        throw new UnsupportedOperationException("This object doesn't support multiplication");
    }

    public XObject<?> add(XObject<?> object) {
        if (this instanceof StringObject || object instanceof StringObject) {
            return new StringObject(this.asString() + object.asString());
        }

        throw new UnsupportedOperationException("This object doesn't support being added to");
    }

    public XObject<?> remove(XObject<?> object) {
        throw new UnsupportedOperationException("This object doesn't support being removed from " + this.getClass() + " | " + object.getClass());
    }

    public XObject<?> power(XObject<?> y) {
        throw new UnsupportedOperationException("This object doesn't support power");
    }

    public boolean lessThan(XObject<?> object) {
        throw new UnsupportedOperationException("This object doesn't support less than action");
    }

    public boolean lessOrEqual(XObject<?> object) {
        throw new UnsupportedOperationException("This object doesn't support less than or equal action");
    }

    public XObject<?> negate() {
        throw new UnsupportedOperationException("This object doesn't support being negated");
    }

    public boolean equalsObj(XObject<?> object) {
        return Objects.equals(this.asJava(), object.asJava());
    }

    public XObject<?> call(XObject<?>... args) {
        throw new UnsupportedOperationException("This object doesn't support being called");
    }

    public void set(XObject<?> key, XObject<?> object) {
        throw new UnsupportedOperationException("This object doesn't support being set");
    }

    public void set(String key, XObject<?> object) {
        throw new UnsupportedOperationException("This object doesn't support being set");
    }

    public XObject<?> get(XObject<?> key) {
        throw new UnsupportedOperationException("This object doesn't support get");
    }

    public XObject<?> get(String key) {
        if (key.equals("string")) {
            if (this.toStringValue == null) {
                this.toStringValue = new StringObject(this.asString());
            }

            return this.toStringValue;
        }

        throw new UnsupportedOperationException("This object doesn't support get");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof XObject<?> xObject ? this.equalsObj(xObject) : false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.asJava());
    }

    public double asNumber() {
        throw new UnsupportedOperationException("This object isn't a number");
    }

    public abstract String asString();

    public XObject<?> and(XObject<?> y) {
        throw new UnsupportedOperationException("This object doesn't support and");
    }

    public XObject<?> or(XObject<?> y) {
        throw new UnsupportedOperationException("This object doesn't support or");
    }
}
