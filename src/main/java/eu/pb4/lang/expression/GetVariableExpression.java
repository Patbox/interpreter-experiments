package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetVariableExpression(String name, int id, Position info) implements SettableExpression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        try {
            return scope.getVariable(this.name, this.id);
        } catch (Throwable e) {
            throw new InvalidOperationException(info, e.getMessage());
        }
    }

    @Override
    public Expression asSetter(Expression value) {
        return new SetVariableExpression(name, id, value,  Position.betweenIn(info, value.info()));
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return new SetVariableOldReturnExpression(name, id, value, Position.betweenIn(info, value.info()));
    }
}
