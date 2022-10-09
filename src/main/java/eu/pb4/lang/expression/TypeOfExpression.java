package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.StringObject;
import eu.pb4.lang.object.XObject;

public record TypeOfExpression(Expression expression, Position info) implements Expression {


    public static Expression of(Expression expression) {
        return new TypeOfExpression(expression, new Position(expression.info().start() - 6, expression.info().end()));
    }

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return new StringObject(this.expression.execute(scope).type());
    }
}
