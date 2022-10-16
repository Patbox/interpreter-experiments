package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;
import eu.pb4.lang.util.GenUtils;

import java.io.DataOutputStream;
import java.io.IOException;

public record SetStringExpression(Expression base, String key, Expression value, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var val = value.execute(scope);
        base.execute(scope).set(scope, key, val, info);
        return val;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        base.writeByteCode(output, objects);
        value.writeByteCode(output, objects);
        output.write(Opcodes.SET_PROPERTY.ordinal());
        GenUtils.writeIdentifierString(output, key);
    }
}
