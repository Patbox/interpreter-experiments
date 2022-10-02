package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetObjectExpression(Expression left, Expression right) implements Expression{
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return left.execute(scope).get(right.execute(scope));
    }
}
