package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.parser.Tokenizer;

public interface Expression {
    Expression NOOP = (x) -> XObject.NULL;

    XObject<?> execute(ObjectScope scope) throws InvalidOperationException;

    default Position info() {
        return Position.EMPTY;
    }

    default XObject<?> thr(String text) throws InvalidOperationException {
        throw new InvalidOperationException(this.info(), text);
    }

    record Position(int start, int end) {
        public static final Position EMPTY = new Position(-1, -1);

        public static Position from(Tokenizer.Token token) {
            return new Position(token.start(), token.end());
        }

        public static Position betweenEx(Position left, Position right) {
            if (left == EMPTY) {
                return right;
            } else if (right == EMPTY) {
                return left;
            }
            return new Position(left.end(), right.start());
        }

        public static Position betweenEx(Tokenizer.Token left, Tokenizer.Token right) {
            return new Position(left.end(), right.start());
        }

        public static Position betweenIn(Position left, Position right) {
            if (left == EMPTY) {
                return right;
            } else if (right == EMPTY) {
                return left;
            }

            return new Position(left.start(), right.end());
        }

        public static Position betweenIn(Tokenizer.Token left, Tokenizer.Token right) {
            return new Position(left.start(), right.end());
        }
    }
}
