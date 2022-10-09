package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetStringOldReturnExpression(Expression base, String key, Expression value, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var obj = base.execute(scope);
        var old = obj.get(scope, key, info);
        obj.set(scope, key, value.execute(scope), info);
        return old;
    }
}
