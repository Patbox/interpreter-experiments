package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.StringMapObject;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public record StringMapExpression(Expression[][] expressions, Position info) implements Expression{
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var list = new StringMapObject();
        for (int i = 0; i < expressions.length; i++) {
            list.asJava().put(expressions[i][0].execute(scope).asString(), expressions[i][1].execute(scope));
        }
        return list;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        for (int i = expressions.length - 1; i >= 0; i--) {
            this.expressions[i][1].writeByteCode(output, objects);
            this.expressions[i][0].writeByteCode(output, objects);
        }
        output.write(Opcodes.PUSH_OBJECT.ordinal());
        output.writeShort(this.expressions.length);
    }
}
