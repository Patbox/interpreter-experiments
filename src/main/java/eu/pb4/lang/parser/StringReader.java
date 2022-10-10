package eu.pb4.lang.parser;

import org.jetbrains.annotations.Nullable;

public class StringReader {
    private final String input;
    private final int[] characters;
    private int index;

    public StringReader(String input) {
        this.input = input;
        this.characters = input.codePoints().toArray();
        this.index = 0;
    }


    public int length() {
        return this.characters.length;
    }

    public boolean isDone() {
        return this.characters.length <= this.index;
    }

    public int index() {
        return this.index;
    }

    public void index(int value) {
        this.index = value;
    }

    public int peek() {
        return this.isDone() ? -1 : this.characters[this.index++];
    }

    public boolean isNext(int character) {
        if (this.peek() == character) {
            return true;
        } else {
            this.back();
            return false;
        }
    }

    public int back() {
        return --this.index;
    }

    @Nullable
    public Result<String> readIdentifier() {
        if (!this.isDone()) {
            int start = this.index;
            int i = this.peek();

            if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122) || i == 95) {
                var builder = new StringBuilder();
                builder.append(Character.toChars(i));
                while (!this.isDone()) {
                    i = this.peek();

                    if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122) || (i >= 48 && i <= 57) || i == 95) {
                        builder.append(Character.toChars(i));
                    } else {
                        return new Result<>(start, this.back(), builder.toString());
                    }
                }
                return new Result<>(start, this.index, builder.toString());
            }

            this.back();
        }

        return null;
    }


        @Nullable
    public Result<String> readString(char quotationType) {
        if (!this.isDone()) {
            int start = this.index;
            int i = this.peek();

            if (i == quotationType) {
                var builder = new StringBuilder();

                while (!this.isDone()) {
                    i = this.peek();

                    if (i == '\\') {
                        builder.append(Character.toChars(i));
                        builder.append(Character.toChars(this.peek()));
                    } else if (i == quotationType) {
                        return new Result<>(start, this.index, builder.toString());
                    } else {
                        builder.append(Character.toChars(i));
                    }
                }
            }

            this.index(start);
        }

        return null;
    }

    private boolean isNumber(int val, int type) {
        if (type <= 10) {
            return val >= '0' && val <= '0' + (type - 1);
        } else {
            return (val >= '0' && val <= '9') || (val >= 'a' && val <= 'a' + (type - 1)) || (val >= 'A' && val <= 'A' + (type - 1));
        }
    }

    @Nullable
    public Result<Double> readDouble() {
        if (!this.isDone()) {
            var builder = new StringBuilder();
            int start = this.index;
            boolean hasDot = false;
            int type = 10;

            var val = this.peek();
            if (val == '-') {
                builder.append('-');
                val = this.peek();
            }

            if (val == '0') {
                val = this.peek();
                if (val == 'x'  || val == 'X') {
                    type = 16;
                } else if (val == 'b' || val == 'B') {
                    type = 2;
                } else if (val == 'o' || val == 'O') {
                    type = 8;
                } else if (val == '.') {
                    hasDot = true;
                    builder.append('0');
                    builder.append('.');
                } else if (isNumber(val, type)) {
                    builder.append('0');
                    builder.append((char) val);
                } else {
                    this.back();
                    return new Result<>(start, this.index, Double.valueOf(0));
                }
            } else if (isNumber(val, type)) {
                builder.append((char) val);
            } else if (val == '.') {
                val = this.peek();
                if (isNumber(val, type) && type == 10) {
                    hasDot = true;
                    builder.append('0');
                    builder.append('.');
                    builder.append((char) val);
                } else {
                    this.index(start);
                    return null;
                }
            } else {
                this.index(start);
                return null;
            }

            while (!this.isDone()) {
                val = this.peek();
                if (this.isNumber(val, type)) {
                    builder.append((char) val);
                } else if (val == '.' && !hasDot && type == 10) {
                    hasDot = true;
                    builder.append('.');
                } else {
                    this.back();
                    try {
                        return new Result<>(start, this.index, hasDot ? Double.parseDouble(builder.toString()) : Integer.parseInt(builder.toString(), type));
                    } catch (NumberFormatException e) {
                        this.index(start);
                        return null;
                    }
                }
            }

            try {
                return new Result<>(start, this.index, hasDot ? Double.parseDouble(builder.toString()) : Integer.parseInt(builder.toString(), type));
            } catch (NumberFormatException e) {
                this.index(start);
            }
        }
        return null;
    }

    public record Result<T>(int from, int to, T value) {};
}
