package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetStringExpression(Expression base, String key, Expression value, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var val = value.execute(scope);
        base.execute(scope).set(scope, key, val, info);
        return val;
    }
}
