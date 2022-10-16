package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.runtime.StaticObjectConsumer;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Expression {
    Expression NOOP = new Expression() {
        @Override
        public XObject<?> execute(ObjectScope scope)  {
            return XObject.NULL;
        }

        @Override
        public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) {}
    };

    XObject<?> execute(ObjectScope scope) throws InvalidOperationException;

    void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException;

    default Position info() {
        return Position.EMPTY;
    }

    default XObject<?> thr(String text) throws InvalidOperationException {
        throw new InvalidOperationException(this.info(), text);
    }

    record Position(int start, int end, String script) {
        public static final Position EMPTY = new Position(-1, -1, "");

        public static Position from(Tokenizer.Token token, String script) {
            return new Position(token.start(), token.end(), script);
        }

        public static Position betweenEx(Position left, Position right) {
            if (left == EMPTY) {
                return right;
            } else if (right == EMPTY) {
                return left;
            }
            return new Position(left.end(), right.start(), right.script);
        }

        public static Position betweenEx(Tokenizer.Token left, Tokenizer.Token right, String script) {
            return new Position(left.end(), right.start(), script);
        }

        public static Position betweenIn(Position left, Position right) {
            if (left == EMPTY) {
                return right;
            } else if (right == EMPTY) {
                return left;
            }

            return new Position(left.start(), right.end(), right.script);
        }

        public static Position betweenIn(Tokenizer.Token left, Tokenizer.Token right, String script) {
            return new Position(left.start(), right.end(), script);
        }
    }
}
