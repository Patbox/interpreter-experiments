package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record ReturnExpression(Expression expression, ForceReturnObject.Type type, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return new ForceReturnObject(expression.execute(scope), type);
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        expression.writeByteCode(output, objects);
        output.write(Opcodes.RETURN.ordinal());
    }
}
