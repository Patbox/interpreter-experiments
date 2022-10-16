package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public record LoopForExpression(List<Expression> initialize, Expression check, Expression increase, List<Expression> executable, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var subScope = new ObjectScope(scope);
        for (var exp : initialize) {
            exp.execute(subScope);
        }

        XObject<?> lastObject = XObject.NULL;

        main:
        while (check.execute(subScope) == BooleanObject.TRUE) {
            for (var e : executable) {
                if (e instanceof LoopSkipExpression loopSkipExpression) {
                    if (loopSkipExpression.shouldBreak()) {
                        break main;
                    } else {
                        break;
                    }
                } else {
                    lastObject = e.execute(subScope);
                    if (lastObject instanceof ForceReturnObject) {
                        return lastObject;
                    }
                }
            }
            increase.execute(subScope);
        }

        return lastObject;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        output.write(Opcodes.SCOPE_UP.ordinal());

        for (var expr : initialize) {
            expr.writeByteCode(output, objects);
        }
        output.write(Opcodes.SCOPE_STATE_SAVE.ordinal());
        var base = output.size();
        check.writeByteCode(output, objects);
        output.write(Opcodes.JUMP_IF_FALSE.ordinal());

        byte[] ifCode;

        {
            var bb = new ByteArrayOutputStream();
            var data = new DataOutputStream(bb);
            for (var exp : executable) {
                exp.writeByteCode(data, objects);
            }
            data.write(Opcodes.SCOPE_STATE_RESTORE.ordinal());

            increase.writeByteCode(data, objects);
            data.write(Opcodes.JUMP.ordinal());
            data.writeInt(base - output.size() - bb.size() - 8);

            ifCode = bb.toByteArray();
        }

        output.writeInt(ifCode.length);
        output.write(ifCode);
        output.write(Opcodes.SCOPE_DOWN.ordinal());
    }
}
