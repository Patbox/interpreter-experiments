package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.function.BiFunction;

public record TrinaryExpression(Expression left, Expression middle, Expression right, TriFunction function) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        return this.function.apply(left.execute(scope), middle.execute(scope), right.execute(scope));
    }

    public static TrinaryExpression set(Expression left, Expression middle, Expression right) {
        return new TrinaryExpression(left, middle, right, (a, b, c) -> {
            a.set(b, c);
            return c;
        });
    }

    public static TrinaryExpression setRetOld(Expression left, Expression middle, Expression right) {
        return new TrinaryExpression(left, middle, right, (a, b, c) -> {
            var old = a.get(b);
            a.set(b, c);
            return old;
        });
    }


    public interface TriFunction {
        XObject<?> apply(XObject<?> left, XObject<?> middle, XObject<?> right);
    }
}
