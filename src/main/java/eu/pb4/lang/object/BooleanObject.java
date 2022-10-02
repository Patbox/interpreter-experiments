package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

public class BooleanObject extends XObject<Boolean> {
    public static final BooleanObject TRUE = new BooleanObject(true);
    public static final BooleanObject FALSE = new BooleanObject(false);


    private final Boolean value;

    private BooleanObject(boolean value) {
        this.value = value;
    }

    public static XObject<?> of(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    @Override
    public String asString() {
        return this.value.toString();
    }

    @Override
    public XObject<?> negate() {
        return value == Boolean.TRUE ? FALSE : TRUE;
    }

    @Override
    public @Nullable Boolean asJava() {
        return Boolean.valueOf(this.value);
    }

    @Override
    public XObject<?> and(XObject<?> y) {
        if (y instanceof BooleanObject booleanObject) {
            return of(this.value && booleanObject.value);
        }

        return super.and(y);
    }

    @Override
    public XObject<?> or(XObject<?> y) {
        if (y instanceof BooleanObject booleanObject) {
            return of(this.value || booleanObject.value);
        }

        return super.and(y);
    }
}
