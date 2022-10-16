package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;
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
    public String type() {
        return "boolean";
    }

    @Override
    public String asString() {
        return this.value.toString();
    }

    @Override
    public XObject<?> negate(ObjectScope scope, Expression.Position info) {
        return value == Boolean.TRUE ? FALSE : TRUE;
    }

    @Override
    public @Nullable Boolean asJava() {
        return Boolean.valueOf(this.value);
    }

    @Override
    public XObject<?> and(ObjectScope scope, XObject<?> y,  Expression.Position info) throws InvalidOperationException {
        if (y instanceof BooleanObject booleanObject) {
            return of(this.value && booleanObject.value);
        }

        return super.and(scope, y, info);
    }

    @Override
    public XObject<?> or(ObjectScope scope, XObject<?> y,  Expression.Position info) throws InvalidOperationException {
        if (y instanceof BooleanObject booleanObject) {
            return of(this.value || booleanObject.value);
        }

        return super.or(scope, y, info);
    }

    @Override
    public boolean asBoolean(Expression.Position info) throws InvalidOperationException {
        return this.value.booleanValue();
    }
}
