package eu.pb4.lang.exception;

import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.util.GenUtils;

public class InvalidOperationException extends Exception implements ScriptConsumer{
    public final Expression.Position position;
    private final String operation;

    private String input;

    public InvalidOperationException(Expression.Position position,  String operation) {
        this.position = position;
        this.operation = operation;
    }

    public void supplyInput(String input) {
        this.input = input;
    }

    @Override
    public String getMessage() {
        if (this.input == null) {
            return "Invalid operation '" + this.operation + "' at index" + position.start() + "/" + position.end() + "!";
        } else {
            var val = GenUtils.getLineAndChar(position.start(), this.input);
            return "Invalid operation '" + this.operation + "' at line " + val[0] + " position " + val[1] + " (\"" + GenUtils.getSubString(this.input, position.start() - 10, position.end() + 10) + "\")!";
        }
    }
}
