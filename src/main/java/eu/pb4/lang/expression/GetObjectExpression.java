package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetObjectExpression(Expression left, Expression right) implements SettableExpression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return left.execute(scope).get(right.execute(scope));
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
