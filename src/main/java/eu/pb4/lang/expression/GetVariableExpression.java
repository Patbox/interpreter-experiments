package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetVariableExpression(String name) implements SettableExpression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return scope.getVariable(this.name);
    }

    @Override
    public Expression asSetter(Expression value) {
        return new SetVariableExpression(name, value);
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return new SetVariableOldReturnExpression(name, value);
    }
}
