package eu.pb4.lang.parser;

import eu.pb4.lang.exception.InvalidTokenException;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final StringReader reader;

    public Tokenizer(StringReader reader) {
        this.reader = reader;
    }


    public List<Token> getTokens() throws InvalidTokenException {
        var list = new ArrayList<Token>();
        main:
        while (!this.reader.isDone()) {

            StringReader.Result<?> result = this.reader.readIdentifier();
            if (result != null) {
                var type = switch ((String) result.value()) {
                    case "if" -> TokenType.IF;
                    case "else" -> TokenType.ELSE;
                    case "return" -> TokenType.RETURN;
                    case "for" -> TokenType.FOR;
                    case "while" -> TokenType.WHILE;
                    case "do" -> TokenType.DO;
                    case "null" -> TokenType.NULL;
                    case "true" -> TokenType.TRUE;
                    case "false" -> TokenType.FALSE;
                    case "var" -> TokenType.DECLARE_VAR;
                    case "break" -> TokenType.BREAK;
                    case "continue" -> TokenType.CONTINUE;
                    case "export" -> TokenType.EXPORT;
                    case "import" -> TokenType.IMPORT;
                    case "class" -> TokenType.CLASS;
                    case "typeof" -> TokenType.TYPEOF;
                    case "async" -> TokenType.ASYNC;
                    case "await" -> TokenType.AWAIT;
                    default -> TokenType.IDENTIFIER;
                };


                list.add(new Token(result.value(), type, result.from(), result.to()));
                continue;
            }

            result = this.reader.readString('"');
            if (result != null) {
                list.add(new Token(result.value(), TokenType.STRING, result.from(), result.to()));
                continue;
            }
            result = this.reader.readString('\'');
            if (result != null) {
                list.add(new Token(result.value(), TokenType.STRING, result.from(), result.to()));
                continue;
            }

            result = this.reader.readDouble();
            if (result != null) {
                list.add(new Token(result.value(), TokenType.NUMBER, result.from(), result.to()));
                continue;
            }

            var i = this.reader.peek();
            int start = this.reader.index();

            switch (i) {
                case ' ', '\n', '\t', '\r' -> {
                    continue;
                }
                default -> {
                    TokenType type = null;

                    if (!this.reader.isDone() && (i == '+' || i == '-' || i == '=' || i == '!' || i == '/' || i == '*' || i == '<' || i == '>' || i == '^' || i == '&' || i == '|')) {
                        var x = this.reader.peek();

                        if (i == '/') {
                            if (x == '/') {
                                while (!this.reader.isDone()) {
                                    if (this.reader.peek() == '\n') {
                                        continue main;
                                    }
                                }
                            } else if (x == '*') {
                                while (!this.reader.isDone()) {
                                    if (this.reader.peek() == '*') {
                                        if (this.reader.peek() == '/') {
                                            continue main;
                                        } else {
                                            this.reader.back();
                                        }
                                    }
                                }
                            }
                        }


                        type = switch (i) {
                            case '+' -> switch (x) {
                                case '=' -> TokenType.ADD_SET;
                                case '+' -> TokenType.INCREASE;
                                default -> null;
                            };
                            case '-' -> switch (x) {
                                case '=' -> TokenType.REMOVE_SET;
                                case '-' -> TokenType.DECREASE;
                                case '>' -> TokenType.FUNCTION_ARROW;
                                default -> null;
                            };

                            case '=' -> '=' == x ? TokenType.EQUAL : null;
                            case '!' -> '=' == x ? TokenType.NEGATE_EQUAL : null;
                            case '/' -> '=' == x ? TokenType.DIVIDE_SET : null;
                            case '*' -> '=' == x ? TokenType.MULTIPLY_SET : null;
                            case '^' -> '=' == x ? TokenType.POWER_SET : null;
                            case '<' -> '=' == x ? TokenType.LESS_OR_EQUAL : '<' == x ? TokenType.SHIFT_LEFT : null;
                            case '>' -> '=' == x ? TokenType.MORE_OR_EQUAL : '>' == x ? TokenType.SHIFT_RIGHT : null;
                            case '&' -> i == x ? TokenType.AND_DOUBLE : null;
                            case '|' -> i == x ? TokenType.OR_DOUBLE : null;
                            default -> null;
                        };


                        if (type == null) {
                            this.reader.back();
                        }
                    }

                    if (type == null) {
                        type = switch (i) {
                            case '+' -> TokenType.ADD;
                            case '-' -> TokenType.REMOVE;
                            case '*' -> TokenType.MULTIPLY;
                            case '%' -> TokenType.DIVIDE_REST;
                            case '/' -> TokenType.DIVIDE;
                            case '^' -> TokenType.POWER;
                            case ';' -> TokenType.END;
                            case '(' -> TokenType.BRACKET_START;
                            case ')' -> TokenType.BRACKET_END;
                            case '[' -> TokenType.SQR_BRACKET_START;
                            case ']' -> TokenType.SQR_BRACKET_END;
                            case '{' -> TokenType.SCOPE_START;
                            case '}' -> TokenType.SCOPE_END;
                            case '=' -> TokenType.SET;
                            case '!' -> TokenType.NEGATE;
                            case '&' -> TokenType.AND;
                            case '|' -> TokenType.OR;
                            case '.' -> TokenType.DOT;
                            case ',' -> TokenType.COMMA;
                            case '<' -> TokenType.LESS_THAN;
                            case '>' -> TokenType.MORE_THAN;
                            case ':' -> TokenType.COLON;
                            case '?' -> TokenType.QUESTION_MARK;
                            default -> null;
                        };
                    }

                    if (type != null) {
                        list.add(new Token(null, type, start, this.reader.index()));
                        continue;
                    } else {
                        throw new InvalidTokenException(start, i);
                    }
                }
            }
        }

        return list;
    }

    public record Token(Object value, TokenType type, int start, int end) {
    }


    public enum TokenType {
        IDENTIFIER,
        STRING,
        NUMBER,
        END,
        ADD,
        ADD_SET,
        INCREASE,
        REMOVE,
        REMOVE_SET,
        DECREASE,
        DIVIDE,
        DIVIDE_SET,
        MULTIPLY,
        MULTIPLY_SET,
        POWER,
        POWER_SET,
        BRACKET_START,
        BRACKET_END,
        SQR_BRACKET_START,
        SQR_BRACKET_END,
        SCOPE_START,
        SCOPE_END,
        NEGATE,
        NEGATE_EQUAL,
        SET,
        DIVIDE_REST,
        EQUAL,

        COLON,
        QUESTION_MARK,

        AND,
        AND_DOUBLE,
        OR,
        OR_DOUBLE,

        SHIFT_LEFT,
        SHIFT_RIGHT,

        DOT,
        COMMA,
        MORE_THAN,
        MORE_OR_EQUAL,
        LESS_THAN,
        LESS_OR_EQUAL,

        DECLARE_VAR,
        IF,
        ELSE,
        RETURN,
        NULL,
        TRUE,
        FALSE,
        FOR,
        WHILE,
        DO,
        EXPORT,
        IMPORT,
        CLASS,
        TYPEOF,
        ASYNC,
        AWAIT,

        CONTINUE,
        BREAK,


        FUNCTION_ARROW
    }
}
