package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.function.BiFunction;

public record JavaExpression(BiFunction<ObjectScope, Position, XObject<?>> function, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return function.apply(scope, this.info);
    }
}
