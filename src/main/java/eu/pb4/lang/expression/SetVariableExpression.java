package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetVariableExpression(String name, Expression expression) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        var val = expression.execute(scope);
        scope.setVariable(this.name, val);
        return val;
    }
}
