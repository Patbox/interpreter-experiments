package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidTokenException;
import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.Tokenizer;

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
}
