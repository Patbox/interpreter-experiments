package eu.pb4.lang.exception;

import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.util.GenUtils;

public class UnexpectedTokenException extends Exception implements ScriptConsumer {
    public final Tokenizer.Token tokenFound;
    public final Tokenizer.TokenType tokenTypeRequired;

    private String input;

    public UnexpectedTokenException(Tokenizer.Token tokenFound, Tokenizer.TokenType typeRequired) {
        this.tokenFound = tokenFound;
        this.tokenTypeRequired = typeRequired;
    }

    public void supplyInput(String input) {
        this.input = input;
    }

    @Override
    public String getMessage() {
        if (this.input == null) {
            return "Invalid token '" + Character.toString(tokenFound.value() instanceof Number n ? n.intValue() : ' ') + "' at index" + tokenFound.start() + "/" + tokenFound.end() + "! Required: "
                    + this.tokenTypeRequired + " Found: " + this.tokenFound.type();
        } else {
            var val = GenUtils.getLineAndChar(tokenFound.start(), this.input);
            return "Invalid token '" + this.input.substring(tokenFound.start(), tokenFound.end() + 1) + "' at line " + val[0] + " position " + val[1] + " (\"" + GenUtils.getSubStringWithoutNewLines(this.input, tokenFound.start() - 10, tokenFound.end() + 10) + "\")! Required: "
                    + this.tokenTypeRequired + " Found: " + this.tokenFound.type();
        }
    }
}
