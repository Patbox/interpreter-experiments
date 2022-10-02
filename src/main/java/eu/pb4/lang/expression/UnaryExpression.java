package eu.pb4.lang.expression;

import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.function.BiFunction;

public record UnaryExpression(Expression left, Expression right, BiFunction<XObject<?>, XObject<?>, XObject<?>> function) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        return this.function.apply(left.execute(scope), right.execute(scope));
    }

    public static UnaryExpression add(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> x.add(y));
    }

    public static UnaryExpression remove(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> x.remove(y));
    }

    public static UnaryExpression multiply(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> x.multiply(y));
    }

    public static UnaryExpression divide(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> x.divide(y));
    }

    public static Expression power(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> x.power(y));
    }

    public static UnaryExpression lessThan(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> BooleanObject.of(x.lessThan(y)));
    }

    public static UnaryExpression lessOrEqual(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> BooleanObject.of(x.lessOrEqual(y)));
    }

    public static UnaryExpression moreThan(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> BooleanObject.of(!x.lessOrEqual(y)));
    }

    public static UnaryExpression moreOrEqual(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> BooleanObject.of(!x.lessThan(y)));
    }

    public static UnaryExpression equal(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> BooleanObject.of(x.equalsObj(y)));
    }

    public static UnaryExpression and(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> x.and(y));
    }

    public static UnaryExpression or(Expression left, Expression right) {
        return new UnaryExpression(left, right, (x, y) -> x.or(y));
    }
}
