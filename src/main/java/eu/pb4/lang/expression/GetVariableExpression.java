package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;
import eu.pb4.lang.util.GenUtils;

import java.io.DataOutputStream;
import java.io.IOException;

public record GetVariableExpression(String name, int id, Position info) implements SettableExpression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        try {
            return this.id != -1 ? scope.getVariable(this.id) : scope.getRuntime().getGlobal(name);
        } catch (Throwable e) {
            throw new InvalidOperationException(info, this.name + e.getMessage());
        }
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        if (id == -1) {
            output.write(Opcodes.GET_GLOBAL.ordinal());
            GenUtils.writeIdentifierString(output, this.name);
        } else {
            output.write(Opcodes.GET_VAR.ordinal());
            output.writeShort(this.id);
        }
    }

    @Override
    public Expression asSetter(Expression value) {
        return new SetVariableExpression(name, id, value,  Position.betweenIn(info, value.info()));
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return new SetVariableOldReturnExpression(name, id, value, Position.betweenIn(info, value.info()));
    }
}
