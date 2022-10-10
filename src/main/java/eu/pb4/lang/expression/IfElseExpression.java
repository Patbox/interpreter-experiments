package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record IfElseExpression(Expression check, List<Expression> left, List<Expression> right, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var executable = check.execute(scope) == BooleanObject.TRUE ? left : right;
        XObject<?> lastObject = XObject.NULL;
        var subScope = new ObjectScope(scope);
        for (var e : executable) {
            lastObject = e.execute(subScope);
            if (lastObject instanceof ForceReturnObject) {
                return lastObject;
            }
        }
        return lastObject;
    }

    public static Expression trinary(Expression left, Expression middle, Expression right) {
        return new IfElseExpression(left, List.of(middle), List.of(right), Expression.Position.betweenIn(left.info(), right.info()));
    }
}
