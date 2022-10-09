package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.function.Function;

public record GetStringExpression(Expression base, String key) implements SettableExpression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        return base.execute(scope).get(key);
    }

    @Override
    public Expression asSetter(Expression value) {
        return new SetStringExpression(base, key, value);
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return new SetStringOldReturnExpression(base, key, value);
    }
}
