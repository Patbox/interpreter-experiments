package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record LoopDoWhileExpression(Expression check, List<Expression> executable, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        XObject<?> lastObject = XObject.NULL;
        var subScope = new ObjectScope(scope);

        do {
            main:
            for (var e : executable) {
                if (e instanceof LoopSkipExpression loopSkipExpression) {
                    if (loopSkipExpression.shouldBreak()) {
                        break main;
                    } else {
                        break;
                    }
                } else {
                    lastObject = e.execute(subScope);
                    if (lastObject instanceof ForceReturnObject) {
                        return lastObject;
                    }
                }
            }
            subScope.clear();
        } while (check.execute(scope) == BooleanObject.TRUE);

        return lastObject;
    }
}
