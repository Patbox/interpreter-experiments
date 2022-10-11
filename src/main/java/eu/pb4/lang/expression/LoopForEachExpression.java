package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record LoopForEachExpression(String identifier, Expression iterator, List<Expression> executable, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {

        var iterable = iterator.execute(scope).iterator(scope, info);
        var lastObject = XObject.NULL;

        while (iterable.hasNext()) {
            var subScope = new ObjectScope(scope);
            subScope.declareVariable(identifier, iterable.next(), false);
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
        }
        return lastObject;
    }
}
