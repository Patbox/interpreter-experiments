package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.StringObject;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record TypeOfExpression(Expression expression, Position info) implements Expression {
    public static Expression of(Expression expression) {
        return new TypeOfExpression(expression, new Position(expression.info().start() - 6, expression.info().end(), expression.info().script()));
    }

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return new StringObject(this.expression.execute(scope).type());
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        expression.writeByteCode(output, objects);
        output.write(Opcodes.TYPE_OF.ordinal());
    }
}
