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

public record IfElseExpression(Expression check, List<Expression> left, List<Expression> right, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var executable = check.execute(scope) == BooleanObject.TRUE ? left : right;
        XObject<?> lastObject = XObject.NULL;
        var subScope = new ObjectScope(scope);
        for (var e : executable) {
            lastObject = e.execute(subScope);
            if (lastObject instanceof ForceReturnObject) {
                return lastObject;
            }
        }
        return lastObject;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        check.writeByteCode(output, objects);
        output.write(Opcodes.JUMP_IF_FALSE.ordinal());

        byte[] ifCode;
        byte[] elseCode;

        {
            var bb = new ByteArrayOutputStream();
            var data = new DataOutputStream(bb);
            data.write(Opcodes.SCOPE_UP.ordinal());
            for (var exp : right) {
                exp.writeByteCode(data, objects);
            }
            data.write(Opcodes.SCOPE_DOWN.ordinal());

            elseCode = bb.toByteArray();
        }

        {
            var bb = new ByteArrayOutputStream();
            var data = new DataOutputStream(bb);
            data.write(Opcodes.SCOPE_UP.ordinal());
            for (var exp : left) {
                exp.writeByteCode(data, objects);
            }
            data.write(Opcodes.SCOPE_DOWN.ordinal());

            ifCode = bb.toByteArray();
        }


        if (elseCode.length > 0) {
            output.writeInt(ifCode.length + 1 + 4);
            output.write(ifCode);

            output.write(Opcodes.JUMP.ordinal());
            output.writeInt(elseCode.length);

            output.write(elseCode);
        } else {
            output.writeInt(ifCode.length);
            output.write(ifCode);
        }
    }

    public static Expression trinary(Expression left, Expression middle, Expression right) {
        return new IfElseExpression(left, List.of(middle), List.of(right), Expression.Position.betweenIn(left.info(), right.info()));
    }
}
