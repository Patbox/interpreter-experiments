package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record AsyncExpression(List<Expression> expr, Position from) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        scope.getRuntime().executeAsync(expr, new ObjectScope(scope));

        return XObject.NULL;
    }
}
