package eu.pb4.lang.expression;

import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record LoopSkipExpression(boolean shouldBreak, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return XObject.NULL;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {

    }
}
