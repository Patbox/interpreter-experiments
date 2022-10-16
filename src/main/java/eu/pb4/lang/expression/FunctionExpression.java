package eu.pb4.lang.expression;

import eu.pb4.lang.object.FunctionObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public record FunctionExpression(List<String> args, int[] argIds, List<Expression> expressions, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return new FunctionObject(scope, args, argIds, expressions);
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        output.write(Opcodes.PUSH_FUNCTION.ordinal());
        output.write(argIds.length);
        for (int i = 0; i < argIds.length; i++) {
            output.writeShort(argIds[i]);
        }

        var bb = new ByteArrayOutputStream();
        var data = new DataOutputStream(bb);

        for (var expr : expressions) {
            expr.writeByteCode(data, objects);
        }

        output.writeInt(bb.size());
        output.write(bb.toByteArray());
    }
}
