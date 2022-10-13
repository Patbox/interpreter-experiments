package eu.pb4.lang.parser;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TokenReader {
    private final Tokenizer.Token[] tokens;
    private int index = 0;

    public TokenReader(List<Tokenizer.Token> tokens) {
        this.tokens = tokens.toArray(new Tokenizer.Token[0]);
    }

    public int length() {
        return this.tokens.length;
    }

    public boolean isDone() {
        return this.length() <= this.index;
    }

    public int index() {
        return this.index;
    }

    public void index(int value) {
        this.index = value;
    }

    public Tokenizer.Token peek() {
        return this.isDone() ? null : this.tokens[this.index++];
    }

    public int back() {
        return --this.index;
    }

    public Tokenizer.Token previous() {
        return this.tokens[this.index - 1];
    }
}
