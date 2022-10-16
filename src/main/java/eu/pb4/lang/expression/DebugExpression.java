package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;
import eu.pb4.lang.util.GenUtils;

import java.io.DataOutputStream;
import java.io.IOException;

public record DebugExpression(String key, Expression.Position info) implements Expression  {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return XObject.NULL;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        output.write(Opcodes.DEBUG.ordinal());
        GenUtils.writeIdentifierString(output, this.key);
    }
}
