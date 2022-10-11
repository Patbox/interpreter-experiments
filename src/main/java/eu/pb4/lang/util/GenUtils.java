package eu.pb4.lang.util;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.object.XObject;

public class GenUtils {
    public static int[] getLineAndChar(int index, String string) {
        var arr = string.codePoints().toArray();

        int line = 1;
        int pos = 1;

        for (int i = 0; i < index; i++) {
            if (arr[i] == '\n') {
                line++;
                pos = 1;
            } else {
                pos++;
            }
        }

        return new int[] { line, pos };
    }

    public static String getSubStringWithoutNewLines(String string, int start, int stop) {
        return string.substring(Math.max(0, start), Math.min(stop, string.length())).replace("\n", " | ");
    }

    public static void argumentCount(XObject<?>[] args, int count, Expression.Position position) throws InvalidOperationException {
        if (args.length < count) {
            throw new InvalidOperationException(position, "Function requires at least " + count + " arguments, found " + args.length);
        }
    }
}
