package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetVariableExpression(String name, Expression expression, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var val = expression.execute(scope);
        try {
            scope.setVariable(this.name, val);
        } catch (Throwable e) {
            throw new InvalidOperationException(info, e.getMessage());
        }
        return val;
    }
}
