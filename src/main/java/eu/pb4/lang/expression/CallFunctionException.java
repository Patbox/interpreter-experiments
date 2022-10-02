package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record CallFunctionException(Expression expression, Expression[] expressions) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        var args = new XObject<?>[expressions.length];

        for (int i = 0; i < this.expressions.length; i++) {
            args[i] = this.expressions[i].execute(scope);
        }

        return expression.execute(scope).call(args);
    }
}
