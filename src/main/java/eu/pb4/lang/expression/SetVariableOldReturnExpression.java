package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetVariableOldReturnExpression(String name, Expression expression) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        var val = scope.getVariable(this.name);
        scope.setVariable(this.name, expression.execute(scope));
        return val;
    }
}
