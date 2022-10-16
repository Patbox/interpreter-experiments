package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.*;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public record LoopWhileExpression(Expression check, List<Expression> executable, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        XObject<?> lastObject = XObject.NULL;
        var subScope = new ObjectScope(scope);

        while (check.execute(scope) == BooleanObject.TRUE) {
            main:
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
        }

        return lastObject;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        var base = output.size();
        check.writeByteCode(output, objects);
        output.write(Opcodes.JUMP_IF_FALSE.ordinal());

        byte[] ifCode;

        {
            var bb = new ByteArrayOutputStream();
            var data = new DataOutputStream(bb);
            data.write(Opcodes.SCOPE_UP.ordinal());
            for (var exp : executable) {
                exp.writeByteCode(data, objects);
            }
            data.write(Opcodes.SCOPE_DOWN.ordinal());
            data.write(Opcodes.JUMP.ordinal());
            data.writeInt(base - output.size() - bb.size() - 8);


            ifCode = bb.toByteArray();
        }

        output.writeInt(ifCode.length);
        output.write(ifCode);
    }
}
