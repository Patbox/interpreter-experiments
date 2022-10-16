package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record GetObjectExpression(Expression left, Expression right, Position info) implements SettableExpression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return left.execute(scope).get(scope, right.execute(scope), info);
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        left.writeByteCode(output, objects);
        right.writeByteCode(output, objects);
        output.write(Opcodes.GET.ordinal());
    }

    @Override
    public Expression asSetter(Expression value) {
        return new TrinaryExpression.Set(left, right, value);
    }

    @Override
    public Expression asSetterWithOldReturn(Expression value) {
        return new TrinaryExpression.SetRetOld(left, right, value);
    }
}
