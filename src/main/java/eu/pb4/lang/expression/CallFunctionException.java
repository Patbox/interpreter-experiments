package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record CallFunctionException(Expression expression, Expression[] expressions, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var args = new XObject<?>[expressions.length];

        for (int i = 0; i < this.expressions.length; i++) {
            args[i] = this.expressions[i].execute(scope);
        }

        return expression.execute(scope).call(scope, args, info);
    }
}
