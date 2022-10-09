package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidTokenException;
import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.Tokenizer;

public record DirectObjectExpression(XObject<?> object) implements Expression {
    public static Expression fromToken(Tokenizer.Token token) throws InvalidTokenException {
        return new DirectObjectExpression(switch (token.type()) {
            case STRING -> new StringObject((String) token.value());
            case NUMBER -> new NumberObject((double) token.value());
            case TRUE -> BooleanObject.TRUE;
            case FALSE -> BooleanObject.FALSE;
            case NULL -> XObject.NULL;
            default -> throw new InvalidTokenException(token);
        });
    }

    @Override
    public XObject<?> execute(ObjectScope scope) {
        return this.object;
    }
}
