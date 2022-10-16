package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;
import eu.pb4.lang.util.GenUtils;

import java.io.DataOutputStream;
import java.io.IOException;

public record GetStringExpression(Expression base, String key, Position info) implements SettableExpression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return base.execute(scope).get(scope, key, info);
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        base.writeByteCode(output, objects);
        output.write(Opcodes.GET_PROPERTY.ordinal());
        GenUtils.writeIdentifierString(output, key);

    }

    @Override
    public Expression asSetter(Expression value) {
        return new SetStringExpression(base, key, value, Position.betweenEx(info, value.info()));
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return new SetStringOldReturnExpression(base, key, value, Position.betweenEx(info, value.info()));
    }
}
