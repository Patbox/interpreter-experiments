package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record CallFunctionException(Expression expression, Expression[] expressions, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var args = new XObject<?>[expressions.length];

        for (int i = 0; i < this.expressions.length; i++) {
            args[i] = this.expressions[i].execute(scope);
        }

        return expression.execute(scope).call(scope, args, info);
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        expression.writeByteCode(output, objects);
        for (var i = expressions.length - 1; i >= 0; i--) {
            expressions[i].writeByteCode(output, objects);
        }

        output.write(Opcodes.CALL_FUNC.ordinal());
        output.write(this.expressions.length);
    }
}
