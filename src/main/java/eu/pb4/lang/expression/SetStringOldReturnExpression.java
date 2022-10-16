package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;
import eu.pb4.lang.util.GenUtils;

import java.io.DataOutputStream;
import java.io.IOException;

public record SetStringOldReturnExpression(Expression base, String key, Expression value, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var obj = base.execute(scope);
        var old = obj.get(scope, key, info);
        obj.set(scope, key, value.execute(scope), info);
        return old;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        base.writeByteCode(output, objects);
        value.writeByteCode(output, objects);
        output.write(Opcodes.SET_PROPERTY_OLD.ordinal());
        GenUtils.writeIdentifierString(output, key);
    }
}
