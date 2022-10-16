package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public record LoopDoWhileExpression(Expression check, List<Expression> executable, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        XObject<?> lastObject = XObject.NULL;
        var subScope = new ObjectScope(scope);

        do {
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
        } while (check.execute(scope) == BooleanObject.TRUE);

        return lastObject;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        var base = output.size();
        output.write(Opcodes.SCOPE_UP.ordinal());
        for (var exp : executable) {
            exp.writeByteCode(output, objects);
        }
        output.write(Opcodes.SCOPE_DOWN.ordinal());

        check.writeByteCode(output, objects);
        output.write(Opcodes.JUMP_IF_TRUE.ordinal());
        output.writeInt(base - output.size() - 4);
    }
}
