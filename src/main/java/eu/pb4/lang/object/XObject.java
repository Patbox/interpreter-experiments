package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.DirectObjectExpression;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

public abstract class XObject<T> {
    public static final XObject<?> NULL = new XObject<>() {
        @Override
        public String asString() {
            return "<null>";
        }

        @Override
        public String type() {
            return "null";
        }

        @Override
        public Object asJava() {
            return null;
        }
    };

    public static final XObject<?> VOID = new XObject<>() {
        @Override
        public String asString() {
            return "<void>";
        }

        @Override
        public String type() {
            return "void";
        }

        @Override
        public Object asJava() {
            return null;
        }
    };

    private StringObject toStringValue;

    @Nullable
    public abstract T asJava();

    public Expression asExpression(Expression.Position info) {
        return new DirectObjectExpression(this, info);
    }


    public XObject<?> divide(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "division of " + this.type() + " with " + object.type());
    }

    public String type() {
        return this.getClass().getSimpleName();
    }

    public XObject<?> multiply(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "multiplication of " + this.type() + " with " + object.type());
    }

    public XObject<?> add(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (this instanceof StringObject || object instanceof StringObject) {
            return new StringObject(this.asString() + object.asString());
        }

        throw new InvalidOperationException(info, "addition of " + this.type() + " with " + object.type());
    }

    public XObject<?> subtract(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "subtraction of " + this.type() + " with " + object.type());
    }

    public XObject<?> power(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "power of " + this.type() + " with " + object.type());
    }

    public boolean lessThan(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "less than check of " + this.type() + " against " + object.type());
    }

    public boolean lessOrEqual(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "less or equal than check of " + this.type() + " against " + object.type());
    }

    public XObject<?> negate(ObjectScope scope, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "negation");
    }

    public boolean equalsObj(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        return Objects.equals(this.asJava(), object.asJava());
    }

    public XObject<?> call(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "being called as function");
    }

    public void set(ObjectScope scope, XObject<?> key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "changing " + this.type() + "'s entry '" + key.asString() + "' (" + key.type() + ") + to " + object.type());
    }

    public void set(ObjectScope scope, String key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "changing " + this.type() + "'s property '" + key + "' to " + object.type());
    }

    public XObject<?> get(ObjectScope scope, XObject<?> key, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, this.type() + "'s doesn't contain entry '" + key.asString() + "' (" + key.type() + ")");
    }


    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        if (key.equals("string")) {
            if (this.toStringValue == null) {
                this.toStringValue = new StringObject(this.asString());
            }

            return this.toStringValue;
        }

        throw new InvalidOperationException(info, this.type() + "doesn't contain property '" + key + "'");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof XObject<?> xObject && Objects.equals(this.asJava(), xObject.asJava());
    }

    public XObject<?> and(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "'and' operator of " + this.type() + " with " + object.type());
    }

    public XObject<?> or(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "'or' operator of " + this.type() + " with " + object.type());
    }

    public Iterator<XObject<?>> iterator(ObjectScope scope, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, this.type() + " isn't iterable");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.asJava());
    }

    public double asDouble() {
        throw new UnsupportedOperationException(this.getClass() + " / " + this.type() + " isn't a number!");
    }

    public abstract String asString();

    public int asInt() {
        return (int) this.asDouble();
    }


    public XObject<?> divideRest(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "rest division of " + this.type() + " with " + object.type());
    }

    public XObject<?> shiftRight(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "shifting right of " + this.type() + " with " + object.type());
    }

    public XObject<?> shiftLeft(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        throw new InvalidOperationException(info, "shifting right of " + this.type() + " with " + object.type());
    }
}
