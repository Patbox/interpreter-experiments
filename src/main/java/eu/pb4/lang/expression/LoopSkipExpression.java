package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record LoopSkipExpression(boolean shouldBreak, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return XObject.NULL;
    }
}
