package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.BooleanObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class UnaryExpression implements Expression {
    private final Opcodes opcode;
    private final Expression left;
    private final Expression right;
    private final Position info;

    public UnaryExpression(Opcodes opcode, Expression left, Expression right) {
        this.opcode = opcode;
        this.left = left;
        this.right = right;
        this.info = Position.betweenEx(left.info(), right.info());
    }

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return this.apply(scope, left.execute(scope), right.execute(scope), info);
    }

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        right.writeByteCode(output, objects);
        left.writeByteCode(output, objects);

        output.write(this.opcode.ordinal());
    }

    abstract protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException ;

    public static class Add extends UnaryExpression {
        public Add(Expression left, Expression right) {
            super(Opcodes.ADD, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.add(scope, right, info);
        }
    }

    public static class Subtract extends UnaryExpression {
        public Subtract(Expression left, Expression right) {
            super(Opcodes.SUBTRACT, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.subtract(scope, right, info);
        }
    }

    public static class Multiply extends UnaryExpression {
        public Multiply(Expression left, Expression right) {
            super(Opcodes.MULTIPLY, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.multiply(scope, right, info);
        }
    }

    public static class Divide extends UnaryExpression {
        public Divide(Expression left, Expression right) {
            super(Opcodes.DIVIDE, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.divide(scope, right, info);
        }
    }

    public static class Power extends UnaryExpression {
        public Power(Expression left, Expression right) {
            super(Opcodes.POWER, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.power(scope, right, info);
        }
    }

    public static class LessThan extends UnaryExpression {
        public LessThan(Expression left, Expression right) {
            super(Opcodes.LESS_THAN, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(left.lessThan(scope, right, info));
        }
    }

    public static class LessOrEqual extends UnaryExpression {
        public LessOrEqual(Expression left, Expression right) {
            super(Opcodes.LESS_OR_EQUAL, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(left.lessOrEqual(scope, right, info));
        }
    }

    public static class MoreThan extends UnaryExpression {
        public MoreThan(Expression left, Expression right) {
            super(Opcodes.LESS_OR_EQUAL, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(!left.lessOrEqual(scope, right, info));
        }

        @Override
        public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
            super.writeByteCode(output, objects);
            output.write(Opcodes.NEGATE.ordinal());
        }
    }

    public static class MoreOrEqual extends UnaryExpression {
        public MoreOrEqual(Expression left, Expression right) {
            super(Opcodes.LESS_THAN, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(!left.lessThan(scope, right, info));
        }

        @Override
        public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
            super.writeByteCode(output, objects);
            output.write(Opcodes.NEGATE.ordinal());
        }
    }

    public static class Equal extends UnaryExpression {
        public Equal(Expression left, Expression right) {
            super(Opcodes.EQUAL, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return BooleanObject.of(left.equalsObj(scope, right, info));
        }
    }

    public static class And extends UnaryExpression {
        public And(Expression left, Expression right) {
            super(Opcodes.AND, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.and(scope, right, info);
        }
    }

    public static class Or extends UnaryExpression {
        public Or(Expression left, Expression right) {
            super(Opcodes.OR, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.or(scope, right, info);
        }
    }

    public static class DivideRest extends UnaryExpression {
        public DivideRest(Expression left, Expression right) {
            super(Opcodes.DIVIDE_REST, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.divideRest(scope, right, info);
        }
    }

    public static class ShiftLeft extends UnaryExpression {
        public ShiftLeft(Expression left, Expression right) {
            super(Opcodes.SHIFT_LEFT, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.shiftLeft(scope, right, info);
        }
    }

    public static class ShiftRight extends UnaryExpression {
        public ShiftRight(Expression left, Expression right) {
            super(Opcodes.SHIFT_RIGHT, left, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> right, Position info) throws InvalidOperationException  {
            return left.shiftRight(scope, right, info);
        }
    }
}
