package eu.pb4.lang.expression;

import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record ReturnExpression(Expression expression) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return new ForceReturnObject(expression.execute(scope));
    }
}
