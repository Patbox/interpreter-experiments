package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record NegateExpression(Expression expression) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return expression.execute(scope).negate();
    }
}
