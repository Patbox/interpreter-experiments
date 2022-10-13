package eu.pb4.lang.exception;

import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.util.GenUtils;

public class InvalidOperationException extends Exception {
    public final Expression.Position position;
    private final String operation;
    
    public InvalidOperationException(Expression.Position position, String operation) {
        this.position = position;
        this.operation = operation;
    }
    
    @Override
    public String getMessage() {
        var val = GenUtils.getLineAndChar(position.start(), position.script());
        return "Invalid operation '" + this.operation + "' at line " + val[0] + " position " + val[1] + " (\"" + GenUtils.getSubStringWithoutNewLines(position.script(), position.start() - 10, position.end() + 10) + "\")!";
    }
}
