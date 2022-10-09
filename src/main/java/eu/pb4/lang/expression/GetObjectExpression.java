package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetObjectExpression(Expression left, Expression right, Position info) implements SettableExpression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return left.execute(scope).get(scope, right.execute(scope), info);
    }

    @Override
    public Expression asSetter(Expression value) {
        return TrinaryExpression.set(left, right, value);
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return TrinaryExpression.setRetOld(left, right, value);
    }
}
