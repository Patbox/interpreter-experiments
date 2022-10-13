package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public abstract class UnaryExpression implements Expression {

    private final Expression left;
    private final Expression right;
    private final Position info;

    public UnaryExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        this.info = Position.betweenEx(left.info(), right.info());
    }

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return this.apply(scope, left.execute(scope), right.execute(scope), info);
    }

    abstract protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException ;

    public static class Add extends UnaryExpression {
        public Add(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.add(scope, right, info);
        }
    }

    public static class Subtract extends UnaryExpression {
        public Subtract(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.subtract(scope, right, info);
        }
    }

    public static class Multiply extends UnaryExpression {
        public Multiply(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.multiply(scope, right, info);
        }
    }

    public static class Divide extends UnaryExpression {
        public Divide(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.divide(scope, right, info);
        }
    }

    public static class Power extends UnaryExpression {
        public Power(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.power(scope, right, info);
        }
    }

    public static class LessThan extends UnaryExpression {
        public LessThan(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(left.lessThan(scope, right, info));
        }
    }

    public static class LessOrEqual extends UnaryExpression {
        public LessOrEqual(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(left.lessOrEqual(scope, right, info));
        }
    }

    public static class MoreThan extends UnaryExpression {
        public MoreThan(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(!left.lessOrEqual(scope, right, info));
        }
    }

    public static class MoreOrEqual extends UnaryExpression {
        public MoreOrEqual(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(!left.lessThan(scope, right, info));
        }
    }

    public static class Equal extends UnaryExpression {
        public Equal(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(left.equalsObj(scope, right, info));
        }
    }

    public static class And extends UnaryExpression {
        public And(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.and(scope, right, info);
        }
    }

    public static class Or extends UnaryExpression {
        public Or(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.or(scope, right, info);
        }
    }

    public static class DivideRest extends UnaryExpression {
        public DivideRest(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.divideRest(scope, right, info);
        }
    }

    public static class ShiftLeft extends UnaryExpression {
        public ShiftLeft(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.shiftLeft(scope, right, info);
        }
    }

    public static class ShiftRight extends UnaryExpression {
        public ShiftRight(Expression left, Expression right) {
            super(left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.shiftRight(scope, right, info);
        }
    }
}
