package eu.pb4.lang.parser;

import eu.pb4.lang.exception.UnexpectedTokenException;
import eu.pb4.lang.expression.*;
import eu.pb4.lang.object.XObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExpressionBuilder {

    private final ExpressionMatcher matcher;

    public ExpressionBuilder(ExpressionMatcher matcher) {
        this.matcher = matcher;
    }


    public List<Expression> build() throws UnexpectedTokenException {
        var list = new ArrayList<Expression>();
        while (!this.matcher.isDone()) {
            parseMultiExpression(list::add);
        }

        return list;
    }


    public void parseMultiExpression(Consumer<Expression> consumer) throws UnexpectedTokenException {
        var token = this.matcher.peek();

        switch (token.type()) {
            case DECLARE_VAR -> parseVariableDefinition(consumer);
            default -> consumer.accept(parseExpression(token));
        }
    }

    public Expression parseExpression() throws UnexpectedTokenException {
        return parseExpression(this.matcher.peek());
    }

    public Expression parseExpression(Tokenizer.Token token) throws UnexpectedTokenException {
        if (token.type() == Tokenizer.TokenType.BRACKET_START) {
            var index = this.matcher.index();
            var args = new ArrayList<String>();

            while (!this.matcher.isDone()) {
                var next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.IDENTIFIER) {
                    args.add((String) next.value());

                    next = this.matcher.peek();

                    if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                        if (this.matcher.peek().type() == Tokenizer.TokenType.FUNCTION_ARROW) {
                            return new FunctionExpression(args, parseScoped());
                        }
                        break;
                    } else if (next.type() == Tokenizer.TokenType.COMMA) {
                        if (this.matcher.peek().type() == Tokenizer.TokenType.IDENTIFIER) {
                            this.matcher.back();
                            continue;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                    if (this.matcher.peek().type() == Tokenizer.TokenType.FUNCTION_ARROW) {
                        return new FunctionExpression(args, parseScoped());
                    }
                }
            }

            this.matcher.index(index);
        }

        return switch (token.type()) {
            case IDENTIFIER, STRING, NUMBER, TRUE, FALSE, NULL, BRACKET_START -> parseValueToken(token);
            case RETURN -> new ReturnExpression(parseExpression());
            case CONTINUE -> new LoopSkipExpression(false);
            case BREAK -> new LoopSkipExpression(true);
            case WHILE -> {
                var next = this.matcher.peek();
                Expression expression;
                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    expression = parseExpression();

                    if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                } else {
                    expression = parseExpression(next);
                }

                var scope = parseScoped();

                yield new LoopWhileExpression(expression, scope);
            }

            case FOR -> {
                var next = this.matcher.peek();
                List<Expression> init = new ArrayList<>();
                Expression expression;
                Expression post;
                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    parseMultiExpression(init::add);
                    if (this.matcher.previous().type() != Tokenizer.TokenType.END && this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                    expression = parseExpression();
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                    post = parseExpression();

                    if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                } else {
                    parseMultiExpression(init::add);
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    expression = parseExpression();
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    post = parseExpression();
                }

                var scope = parseScoped();

                yield new LoopForExpression(init, expression, post, scope);
            }
            case END -> Expression.NOOP;
            default -> throw new UnexpectedTokenException(token, Tokenizer.TokenType.END);
        };
    }

    private List<Expression> parseScoped() throws UnexpectedTokenException {
        var peek = this.matcher.peek();

        if (peek.type() == Tokenizer.TokenType.SCOPE_START) {
            var list = new ArrayList<Expression>();

            while (!this.matcher.isDone()) {
                if (this.matcher.peek().type() == Tokenizer.TokenType.SCOPE_END) {
                    return list;
                } else {
                    this.matcher.back();
                    parseMultiExpression(list::add);
                }
            }
            throw new UnexpectedTokenException(peek, Tokenizer.TokenType.SCOPE_END);
        } else {
            this.matcher.back();
            return List.of(parseExpression());
        }
    }

    private Expression parseValueToken(Tokenizer.Token token) throws UnexpectedTokenException {
        Expression expression = switch (token.type()) {
            case IDENTIFIER -> new GetVariableExpression((String) token.value());
            case BRACKET_START -> {
                var exp = parseExpression();
                var next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                    yield exp;
                } else {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
            }
            default -> DirectObjectExpression.fromToken(token);
        };

        while (!this.matcher.isDone()) {
            var next = this.matcher.peek();

            switch (next.type()) {
                case DOT -> {
                    var key = this.matcher.peek();
                    if (key.type() == Tokenizer.TokenType.IDENTIFIER) {
                        expression = new GetStringExpression(expression, (String) key.value());
                    }
                }

                case ADD -> expression = UnaryExpression.add(expression, parseExpression());
                case REMOVE -> expression = UnaryExpression.remove(expression, parseExpression());
                case MULTIPLY -> expression = UnaryExpression.multiply(expression, parseExpression());
                case DIVIDE -> expression = UnaryExpression.divide(expression, parseExpression());
                case POWER -> expression = UnaryExpression.power(expression, parseExpression());

                case LESS_THAN -> expression = UnaryExpression.lessThan(expression, parseExpression());
                case LESS_OR_EQUAL -> expression = UnaryExpression.lessOrEqual(expression, parseExpression());

                case MORE_THAN -> expression = UnaryExpression.moreThan(expression, parseExpression());
                case MORE_OR_EQUAL -> expression = UnaryExpression.moreOrEqual(expression, parseExpression());

                case EQUAL -> expression = UnaryExpression.equal(expression, parseExpression());
                case NEGATE_EQUAL -> expression = new NegateExpression(UnaryExpression.equal(expression, parseExpression()));

                case AND -> expression = UnaryExpression.and(expression, parseExpression());
                case OR -> expression = UnaryExpression.or(expression, parseExpression());

                case SQR_BRACKET_START -> {
                    expression = new GetObjectExpression(expression, parseExpression(this.matcher.peek()));

                    if (this.matcher.peek().type() != Tokenizer.TokenType.SQR_BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.SQR_BRACKET_END);
                    }
                }

                case BRACKET_START -> {
                    var args = new ArrayList<>();
                    boolean lastIsComma = false;
                    boolean work = true;
                    while (!this.matcher.isDone() && work) {
                        var nexter = this.matcher.peek();


                        switch (nexter.type()) {
                            case BRACKET_END -> {
                                if (lastIsComma) {
                                    throw new UnexpectedTokenException(nexter, Tokenizer.TokenType.BRACKET_END);
                                }

                                expression = new CallFunctionException(expression, args.toArray(new Expression[0]));
                                work = false;
                                break;
                            }
                            case COMMA -> {
                                if (lastIsComma) {
                                    throw new UnexpectedTokenException(nexter, Tokenizer.TokenType.COMMA);
                                }

                                lastIsComma = true;
                            }
                            default -> {
                                lastIsComma = false;
                                args.add(parseExpression(nexter));
                            }
                        }
                    }
                }

                default -> {
                    if (expression instanceof GetObjectExpression getObjectExpression) {
                        switch (next.type()) {
                            case ADD_SET -> expression = TrinaryExpression.set(getObjectExpression.left(), getObjectExpression.right(), UnaryExpression.add(expression, parseExpression()));

                            case REMOVE_SET -> expression = TrinaryExpression.set(getObjectExpression.left(), getObjectExpression.right(), UnaryExpression.remove(expression, parseExpression()));

                            case MULTIPLY_SET -> expression = TrinaryExpression.set(getObjectExpression.left(), getObjectExpression.right(), UnaryExpression.multiply(expression, parseExpression()));

                            case DIVIDE_SET -> expression = TrinaryExpression.set(getObjectExpression.left(), getObjectExpression.right(), UnaryExpression.divide(expression, parseExpression()));

                            case POWER_SET -> expression = TrinaryExpression.set(getObjectExpression.left(), getObjectExpression.right(), UnaryExpression.power(expression, parseExpression()));

                            case SET -> expression = TrinaryExpression.set(getObjectExpression.left(), getObjectExpression.right(), parseExpression());


                            default -> {
                                this.matcher.back();
                                return expression;
                            }
                        }
                    } else if (expression instanceof GetStringExpression getStringExpression) {
                        switch (next.type()) {
                            case ADD_SET -> expression = new SetStringExpression(getStringExpression.base(), getStringExpression.key(), UnaryExpression.add(expression, parseExpression()));

                            case REMOVE_SET -> expression = new SetStringExpression(getStringExpression.base(), getStringExpression.key(), UnaryExpression.remove(expression, parseExpression()));


                            case MULTIPLY_SET -> expression = new SetStringExpression(getStringExpression.base(), getStringExpression.key(), UnaryExpression.multiply(expression, parseExpression()));


                            case DIVIDE_SET -> expression = new SetStringExpression(getStringExpression.base(), getStringExpression.key(), UnaryExpression.divide(expression, parseExpression()));

                            case POWER_SET -> expression = new SetStringExpression(getStringExpression.base(), getStringExpression.key(), UnaryExpression.power(expression, parseExpression()));

                            case SET -> expression = new SetStringExpression(getStringExpression.base(), getStringExpression.key(), parseExpression());

                            default -> {
                                this.matcher.back();
                                return expression;
                            }
                        }
                    } else if (expression instanceof GetVariableExpression variableExpression) {
                        switch (next.type()) {
                            case ADD_SET -> expression = new SetVariableExpression(variableExpression.name(), UnaryExpression.add(expression, parseExpression()));

                            case REMOVE_SET -> expression =  new SetVariableExpression(variableExpression.name(), UnaryExpression.remove(expression, parseExpression()));

                            case MULTIPLY_SET -> expression =  new SetVariableExpression(variableExpression.name(), UnaryExpression.multiply(expression, parseExpression()));

                            case DIVIDE_SET -> expression = new SetVariableExpression(variableExpression.name(), UnaryExpression.divide(expression, parseExpression()));

                            case POWER_SET -> expression = new SetVariableExpression(variableExpression.name(), UnaryExpression.power(expression, parseExpression()));


                            case SET -> expression = new SetVariableExpression(variableExpression.name(), parseExpression());


                            default -> {
                                this.matcher.back();
                                return expression;
                            }
                        }
                    } else {
                        this.matcher.back();
                        return expression;
                    }
                }
            }
        }

        return expression;
    }

    private void parseVariableDefinition(Consumer<Expression> consumer) throws UnexpectedTokenException {
        var id = this.matcher.peek();
        var value = XObject.NULL.asExpression();
        if (id.type() == Tokenizer.TokenType.IDENTIFIER) {
            while (true) {
                var next = this.matcher.peek();
                if (next.type() == Tokenizer.TokenType.END) {
                    consumer.accept(new DefineVariableExpression((String) id.value(), value));
                    break;
                } else if (next.type() == Tokenizer.TokenType.SET) {
                    value = parseExpression();
                } else if (next.type() == Tokenizer.TokenType.COMMA) {
                    consumer.accept(new DefineVariableExpression((String) id.value(), value));
                    id = this.matcher.peek();
                    if (id.type() != Tokenizer.TokenType.IDENTIFIER) {
                        throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                    }

                    value = XObject.NULL.asExpression();
                }
            }
        } else {
            throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
        }
    }
}
