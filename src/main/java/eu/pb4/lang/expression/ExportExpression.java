package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record ExportExpression(Expression key, Expression value, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        scope.addExport(key.execute(scope).asString(), value.execute(scope));
        return XObject.NULL;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        value.writeByteCode(output, objects);
        key.writeByteCode(output, objects);
        output.write(Opcodes.EXPORT.ordinal());
    }
}
