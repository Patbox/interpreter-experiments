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

    @Nullable
    public Result<Double> readDouble() {
        var builder = new StringBuilder();
        int start = this.index;
        boolean initiated = false;
        double value = 0;

        boolean isHex = false;

        while (!this.isDone()) {
            int i = this.peek();
            if (i == 'x' && builder.length() == 1 && builder.charAt(0) == '0') {
                builder = new StringBuilder();
                i = this.peek();
                isHex = true;
            } else if (i == '-' && builder.length() == 0) {
                builder.append(Character.toChars(i));
                i = this.peek();
            }

            builder.append(Character.toChars(i));
            try {
                value = isHex ? Integer.parseInt(builder.toString(), 16) : Double.parseDouble(builder.toString());
                initiated = true;
            } catch (Throwable throwable) {
                if (initiated) {
                    this.back();
                    return new Result<>(start, this.index, value);
                }
            }
        }

        this.index(start);
        return null;
    }

    public record Result<T>(int from, int to, T value) {};
}
