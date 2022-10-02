package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record DefineVariableExpression(String id, Expression value) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        var value = this.value.execute(scope);
        scope.declareVariable(id, value);
        return value;
    }
}
