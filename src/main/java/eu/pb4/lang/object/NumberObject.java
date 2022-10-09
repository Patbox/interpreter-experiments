package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

public class NumberObject extends XObject<Double> {
    private final double value;

    public NumberObject(double value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return this.value - (int) this.value == 0 ? Integer.toString((int) this.value) : Double.toString(this.value);
    }

    @Override
    public @Nullable Double asJava() {
        return this.value;
    }

    public double value() {
        return this.value;
    }

    @Override
    public double asNumber() {
        return this.value;
    }

    @Override
    public XObject<?> add(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value + numberObject.value);
        }

        return super.add(scope, object, info);
    }

    @Override
    public XObject<?> subtract(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value - numberObject.value);
        }

        return super.subtract(scope, object, info);
    }

    @Override
    public XObject<?> multiply(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value * numberObject.value);
        }

        return super.multiply(scope, object, info);
    }

    @Override
    public XObject<?> divide(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value / numberObject.value);
        }

        return super.divide(scope, object, info);
    }

    @Override
    public XObject<?> power(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(Math.pow(this.value, numberObject.value));
        }

        return super.power(scope, object, info);
    }

    @Override
    public boolean lessThan(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            return this.value < numberObject.value;
        }

        return super.lessThan(scope, object, info);
    }

    @Override
    public boolean lessOrEqual(ObjectScope scope, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        if (object instanceof NumberObject numberObject) {
            return this.value <= numberObject.value;
        }

        return super.lessThan(scope, object, info);
    }

    @Override
    public String type() {
        return "number";
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "hexInt" -> new StringObject(Integer.toHexString((int) this.value));
            case "floor" -> new NumberObject(Math.floor(this.value));
            case "ceil" -> new NumberObject(Math.ceil(this.value));
            case "round" -> new NumberObject(Math.round(this.value));
            default -> super.get(scope, key, info);
        };
    }
}
