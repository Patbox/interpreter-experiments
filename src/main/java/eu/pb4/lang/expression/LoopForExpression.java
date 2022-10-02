package eu.pb4.lang.expression;

import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record LoopForExpression(List<Expression> initialize, Expression check, Expression increase, List<Expression> executable) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        var subScopeBase = new ObjectScope(scope);
        for (var exp : initialize) {
            exp.execute(subScopeBase);
        }
        XObject<?> lastObject = XObject.NULL;

        main:
        while (check.execute(subScopeBase) == BooleanObject.TRUE) {
            var subScope = new ObjectScope(subScopeBase);
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
            increase.execute(subScopeBase);
        }

        return lastObject;
    }
}
