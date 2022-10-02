package eu.pb4.lang.expression;

import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.Tokenizer;

public record DirectObjectExpression(XObject<?> object) implements Expression {
    public static Expression fromToken(Tokenizer.Token token) {
        return new DirectObjectExpression(switch (token.type()) {
            case STRING -> new StringObject((String) token.value());
            case NUMBER -> new NumberObject((double) token.value());
            case TRUE -> BooleanObject.TRUE;
            case FALSE -> BooleanObject.FALSE;
            default -> XObject.NULL;
        });
    }

    @Override
    public XObject<?> execute(ObjectScope scope) {
        return this.object;
    }
}
