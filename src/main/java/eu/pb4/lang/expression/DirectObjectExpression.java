package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidTokenException;
import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public record DirectObjectExpression(XObject<?> object, Position info) implements Expression {
    public static Expression fromToken(Tokenizer.Token token, String script) throws InvalidTokenException {
        return new DirectObjectExpression(switch (token.type()) {
            case STRING -> new StringObject(((String) token.value()).replace('\n', '\0').replace("\\n", "\n"));
            case NUMBER -> NumberObject.of((double) token.value());
            case TRUE -> BooleanObject.TRUE;
            case FALSE -> BooleanObject.FALSE;
            case NULL -> XObject.NULL;
            default -> throw new InvalidTokenException(token);
        }, Position.from(token, script));
    }

    @Override
    public XObject<?> execute(ObjectScope scope) {
        return this.object;
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        if (this.object instanceof NumberObject object) {
            if (object.value() == (int) object.value()) {
                output.write(Opcodes.PUSH_INT.ordinal());
                output.writeInt((int) object.value());
            } else {
                output.write(Opcodes.PUSH_DOUBLE.ordinal());
                output.writeDouble(object.value());
            }

        } else if (this.object instanceof BooleanObject object) {
            output.write(Opcodes.PUSH_BOOLEAN.ordinal());
            output.write(object == BooleanObject.TRUE ? 1 : 0);
        } else if (this.object instanceof StringObject object) {
            output.write(Opcodes.PUSH_STRING.ordinal());
            var bytes = object.asJava().getBytes(StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
        } else if (this.object == XObject.NULL) {
            output.write(Opcodes.PUSH_NULL.ordinal());
        } else {
            throw new RuntimeException("NOT SUPPORTED YET");
        }
    }
}
