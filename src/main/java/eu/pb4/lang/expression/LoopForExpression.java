package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record LoopForExpression(List<Expression> initialize, Expression check, Expression increase, List<Expression> executable, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var subScope = new ObjectScope(scope);
        for (var exp : initialize) {
            exp.execute(subScope);
        }

        subScope.updateInitialState();
        XObject<?> lastObject = XObject.NULL;

        main:
        while (check.execute(subScope) == BooleanObject.TRUE) {
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
            increase.execute(subScope);
        }

        return lastObject;
    }
}
