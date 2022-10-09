package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetStringExpression(Expression base, String key, Position info) implements SettableExpression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return base.execute(scope).get(scope, key, info);
    }

    @Override
    public Expression asSetter(Expression value) {
        return new SetStringExpression(base, key, value, Position.betweenEx(info, value.info()));
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return new SetStringOldReturnExpression(base, key, value, Position.betweenEx(info, value.info()));
    }
}
