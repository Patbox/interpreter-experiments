package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record ReturnExpression(Expression expression, ForceReturnObject.Type type, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return new ForceReturnObject(expression.execute(scope), type);
    }
}
