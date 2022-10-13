package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetVariableOldReturnExpression(String name, int id, Expression expression, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var val = scope.getVariable(this.name, id);
        scope.setVariable(this.name, id, expression.execute(scope));
        return val;
    }
}
