package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record DefineVariableExpression(String name, int id, Expression value, boolean readOnly, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var value = this.value.execute(scope);
        try {
            scope.declareVariable(name, id, value, readOnly);
        } catch (Throwable e) {
            throw new InvalidOperationException(info, e.getMessage());
        }
        return value;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        value.writeByteCode(output, objects);

        output.write(Opcodes.DECLARE_VAR.ordinal());
        output.writeShort(id);
        output.write(readOnly ? 1 : 0);
    }
}
