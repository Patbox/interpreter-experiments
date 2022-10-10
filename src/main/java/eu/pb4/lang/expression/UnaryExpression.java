package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record UnaryExpression(Expression left, Expression right, Function function, Position info) implements Expression {


    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return this.function.apply(scope, left.execute(scope), right.execute(scope), info);
    }

    public static UnaryExpression add(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.add(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression remove(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.subtract(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression multiply(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.multiply(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression divide(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.divide(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static Expression power(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.power(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression lessThan(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> BooleanObject.of(x.lessThan(s, y, i)), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression lessOrEqual(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> BooleanObject.of(x.lessOrEqual(s, y, i)), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression moreThan(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> BooleanObject.of(!x.lessOrEqual(s, y, i)), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression moreOrEqual(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> BooleanObject.of(!x.lessThan(s, y, i)), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression equal(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> BooleanObject.of(x.equalsObj(s, y, i)), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression and(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.and(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static UnaryExpression or(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.or(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static Expression divideRest(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.divideRest(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static Expression shiftRight(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.shiftRight(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public static Expression shiftLeft(Expression left, Expression right) {
        return new UnaryExpression(left, right, (s, x, y, i) -> x.shiftLeft(s, y, i), Position.betweenEx(left.info(), right.info()));
    }

    public interface Function {
        XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException;
    }
}
