package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetVariableExpression(String name) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return scope.getVariable(this.name);
    }
}
