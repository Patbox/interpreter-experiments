package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record DefineVariableExpression(String id, Expression value, boolean readOnly, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var value = this.value.execute(scope);
        try {
            scope.declareVariable(id, value, readOnly);
        } catch (Throwable e) {
            throw new InvalidOperationException(info, e.getMessage());
        }
        return value;
    }
}
