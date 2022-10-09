package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetStringOldReturnExpression(Expression base, String key, Expression value) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        var obj = base.execute(scope);
        var old = obj.get(key);
        obj.set(key, value.execute(scope));
        return old;
    }
}
