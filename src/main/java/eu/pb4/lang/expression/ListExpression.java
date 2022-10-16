package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ListObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record ListExpression(Expression[] expressions, Position info) implements Expression{
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var list = new ListObject();
        for (int i = 0; i < expressions.length; i++) {
            list.asJava().add(expressions[i].execute(scope));
        }
        return list;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        for (int i = expressions.length - 1; i >= 0; i--) {
            this.expressions[i].writeByteCode(output, objects);
        }
        output.write(Opcodes.PUSH_LIST.ordinal());
        output.writeShort(this.expressions.length);
    }
}
