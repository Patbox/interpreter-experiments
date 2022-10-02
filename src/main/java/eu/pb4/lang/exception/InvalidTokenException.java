package eu.pb4.lang.exception;

import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.util.GenUtils;

public class InvalidTokenException extends Exception implements  ScriptConsumer{
    private final int start;
    private final int character;

    private String input;

    public InvalidTokenException(int start, int character) {
        this.start = start;
        this.character = character;
    }

    public void supplyInput(String input) {
        this.input = input;
    }

    @Override
    public String getMessage() {
        if (this.input == null) {
            return "Invalid character \"" + Character.toString(this.character) + "\" at index " + this.start + "!";
        } else {
            var val = GenUtils.getLineAndChar(this.start, this.input);
            return "Invalid character \"" + Character.toString(this.character) + "\" at line " + val[0] + " position " + val[1] + " (\"" + GenUtils.getSubString(this.input, this.start - 10, this.start + 10) + "\")!";
        }
    }
}
