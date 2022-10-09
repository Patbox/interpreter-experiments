package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record SetStringExpression(Expression base, String key, Expression value) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        var val = value.execute(scope);
        base.execute(scope).set(key, val);
        return val;
    }
}
