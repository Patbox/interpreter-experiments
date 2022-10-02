package eu.pb4.lang.object;

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
    public XObject<?> add(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value + numberObject.value);
        }

        return super.add(object);
    }

    @Override
    public XObject<?> remove(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value - numberObject.value);
        }

        return super.remove(object);
    }

    @Override
    public XObject<?> multiply(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value * numberObject.value);
        }

        return super.multiply(object);
    }

    @Override
    public XObject<?> divide(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(this.value / numberObject.value);
        }

        return super.divide(object);
    }

    @Override
    public XObject<?> power(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            return new NumberObject(Math.pow(this.value, numberObject.value));
        }

        return super.power(object);
    }

    @Override
    public boolean lessThan(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            return this.value < numberObject.value;
        }

        return super.lessThan(object);
    }

    @Override
    public boolean lessOrEqual(XObject<?> object) {
        if (object instanceof NumberObject numberObject) {
            return this.value <= numberObject.value;
        }

        return super.lessThan(object);
    }

    @Override
    public XObject<?> get(String key) {
        return switch (key) {
            case "hexInt" -> new StringObject(Integer.toHexString((int) this.value));
            case "floor" -> new NumberObject(Math.floor(this.value));
            case "ceil" -> new NumberObject(Math.ceil(this.value));
            case "round" -> new NumberObject(Math.round(this.value));
            default -> super.get(key);
        };
    }
}
