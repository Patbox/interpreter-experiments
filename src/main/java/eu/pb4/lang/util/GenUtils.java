package eu.pb4.lang.util;

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

    public static String getSubString(String string, int start, int stop) {
        return string.substring(Math.max(0, start), Math.min(stop, string.length()));
    }
}
