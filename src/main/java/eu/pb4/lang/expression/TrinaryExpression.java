package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.Opcodes;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class TrinaryExpression implements Expression {

    private final Expression left;
    private final Expression right;
    private final Expression middle;
    private final Position info;
    private final Opcodes opcode;

    public TrinaryExpression(Opcodes opcode, Expression left, Expression middle, Expression right) {
        this.opcode = opcode;
        this.left = left;
        this.middle = middle;
        this.right = right;
        this.info = Position.betweenEx(middle.info(), right.info());
    }

    @Override
    public Position info() {
        return info;
    }

    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        return this.apply(scope, left.execute(scope), middle.execute(scope), right.execute(scope), info);
    }

    protected abstract XObject<?> apply(ObjectScope scope, XObject<?> execute, XObject<?> execute1, XObject<?> execute2, Position info) throws InvalidOperationException;

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {
        left.writeByteCode(output, objects);
        middle.writeByteCode(output, objects);
        right.writeByteCode(output, objects);

        output.write(this.opcode.ordinal());
    }

    public static class Set extends TrinaryExpression {
        public Set(Expression left, Expression middle, Expression right) {
            super(Opcodes.SET, left, middle, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> execute, XObject<?> execute1, XObject<?> execute2, Position info) throws InvalidOperationException {
            execute.set(scope, execute1, execute2, info);
            return execute2;
        }
    }

    public static class SetRetOld extends TrinaryExpression {
        public SetRetOld(Expression left, Expression middle, Expression right) {
            super(Opcodes.SET_OLD, left, middle, right);
        }

        @Override
        protected XObject<?> apply(ObjectScope scope, XObject<?> execute, XObject<?> execute1, XObject<?> execute2, Position info) throws InvalidOperationException {
            var old = execute.get(scope, execute1, info);
            execute.set(scope, execute1, execute2, info);
            return old;
        }
    }


    public interface Function {
        XObject<?> apply(ObjectScope scope, XObject<?> left, XObject<?> middle, XObject<?> right, Position info) throws InvalidOperationException;
    }
}
