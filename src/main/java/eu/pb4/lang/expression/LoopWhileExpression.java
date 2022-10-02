package eu.pb4.lang.expression;

import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.JavaFunctionObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record LoopWhileExpression(Expression check, List<Expression> executable) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        var subScopeBase = new ObjectScope(scope);
        XObject<?> lastObject = XObject.NULL;

        while (check.execute(scope) == BooleanObject.TRUE) {
            var subScope = new ObjectScope(subScopeBase);
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
                    if (e instanceof ReturnExpression) {
                        return lastObject;
                    }
                }
            }
        }

        return lastObject;
    }
}
