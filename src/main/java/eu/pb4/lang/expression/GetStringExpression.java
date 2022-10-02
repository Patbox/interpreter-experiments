package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.function.Function;

public record GetStringExpression(Expression base, String key) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        return base.execute(scope).get(key);
    }
}
