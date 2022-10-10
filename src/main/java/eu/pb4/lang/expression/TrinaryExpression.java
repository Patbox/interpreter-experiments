package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record TrinaryExpression(Expression left, Expression middle, Expression right, Function function, Position info) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return this.function.apply(scope, left.execute(scope), middle.execute(scope), right.execute(scope), info);
    }

    public static TrinaryExpression set(Expression left, Expression middle, Expression right) {
        return new TrinaryExpression(left, middle, right, (s, a, b, c, i) -> {
            a.set(s, b, c, i);
            return c;
        }, Position.betweenEx(middle.info(), right.info()));
    }

    public static TrinaryExpression setRetOld(Expression left, Expression middle, Expression right) {
        return new TrinaryExpression(left, middle, right, (s, a, b, c, i) -> {
            var old = a.get(s, b, i);
            a.set(s, b, c, i);
            return old;
        }, Position.betweenEx(middle.info(), right.info()));
    }


    public interface Function {
        XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> middle, XObject<?> right, Position info) throws InvalidOperationException;
    }
}
