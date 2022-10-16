package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;
import eu.pb4.lang.util.GenUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public record LoopForEachExpression(String identifier, int id, Expression iterator, List<Expression> executable, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {

        var iterable = iterator.execute(scope).iterator(scope, info);
        var lastObject = XObject.NULL;
        var subScope = new ObjectScope(scope);
        subScope.declareVariable(identifier, id, XObject.NULL, false);

        while (iterable.hasNext()) {
            subScope.setVariable(id, iterable.next());
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
        iterator.writeByteCode(output, objects);
        output.write(Opcodes.ITERATOR.ordinal());

        var base = output.size();
        output.write(Opcodes.DUPLICATE_TOP.ordinal());

        output.write(Opcodes.GET_PROPERTY.ordinal());
        GenUtils.writeIdentifierString(output, "hasNext");

        output.write(Opcodes.JUMP_IF_FALSE.ordinal());

        byte[] ifCode;

        {
            var bb = new ByteArrayOutputStream();
            var data = new DataOutputStream(bb);

            data.write(Opcodes.SCOPE_UP.ordinal());

            output.write(Opcodes.DUPLICATE_TOP.ordinal());
            output.write(Opcodes.GET_PROPERTY.ordinal());
            GenUtils.writeIdentifierString(output, "next");

            data.write(Opcodes.DECLARE_VAR.ordinal());
            GenUtils.writeIdentifierString(output, this.identifier);
            data.writeShort(id);
            data.write(0);

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
