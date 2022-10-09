package eu.pb4.lang.parser;

import eu.pb4.lang.exception.InvalidTokenException;
import eu.pb4.lang.exception.UnexpectedTokenException;
import eu.pb4.lang.expression.*;
import eu.pb4.lang.object.NumberObject;
import eu.pb4.lang.object.XObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExpressionBuilder {

    private final ExpressionMatcher matcher;

    public ExpressionBuilder(ExpressionMatcher matcher) {
        this.matcher = matcher;
    }


    public List<Expression> build() throws UnexpectedTokenException, InvalidTokenException {
        var list = new ArrayList<Expression>();
        while (!this.matcher.isDone()) {
            parseMultiExpression(list::add);
        }
        list.removeIf(x -> x == Expression.NOOP);

        return list;
    }


    public void parseMultiExpression(Consumer<Expression> consumer) throws UnexpectedTokenException, InvalidTokenException {
        var token = this.matcher.peek();

        switch (token.type()) {
            case DECLARE_VAR -> parseVariableDefinition(consumer);
            default -> consumer.accept(parseExpression(token));
        }
    }

    public Expression parseExpression() throws UnexpectedTokenException, InvalidTokenException {
        return parseExpression(this.matcher.peek());
    }

    public Expression parseExpression(Tokenizer.Token token) throws UnexpectedTokenException, InvalidTokenException {
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
                } else {
                    break;
                }
            }

            this.matcher.index(index);
        }

        return switch (token.type()) {
            case IDENTIFIER, STRING, NUMBER, TRUE, FALSE, NULL, BRACKET_START, INCREASE, DECREASE -> parseValueToken(token);
            case EXPORT -> {
                var next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.IDENTIFIER) {
                    yield new ExportExpression((String) next.value(), parseExpression());
                } else {
                    throw new UnexpectedTokenException(next, Tokenizer.TokenType.IDENTIFIER);
                }
            }

            case IMPORT -> {
                var id = this.matcher.peek();
                var path = this.matcher.peek();

                if (id.type() == Tokenizer.TokenType.IDENTIFIER && path.type() == Tokenizer.TokenType.STRING) {
                    yield new DefineVariableExpression((String) id.value(), new ImportExpression((String) path.value()));
                } else {
                    throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                }
            }
            case RETURN -> new ReturnExpression(parseEmptyExpression());
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
                    this.matcher.back();
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
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    expression = parseExpression();
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }

                    post = parseExpression();
                    if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                } else {
                    this.matcher.back();
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

    private Expression parseEmptyExpression() throws UnexpectedTokenException, InvalidTokenException {
        var next = this.matcher.peek();

        if (next.type() == Tokenizer.TokenType.END) {
            return Expression.NOOP;
        } else {
            return this.parseExpression(next);
        }
    }

    private List<Expression> parseScoped() throws UnexpectedTokenException, InvalidTokenException {
        var peek = this.matcher.peek();

        if (peek.type() == Tokenizer.TokenType.SCOPE_START) {
            var list = new ArrayList<Expression>();

            while (!this.matcher.isDone()) {
                var token = this.matcher.peek();
                if (token.type() == Tokenizer.TokenType.SCOPE_END) {
                    return list;
                } else {
                    this.matcher.back();
                    parseMultiExpression(list::add);
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                }
            }
            throw new UnexpectedTokenException(peek, Tokenizer.TokenType.SCOPE_END);
        } else {
            this.matcher.back();
            return List.of(parseExpression());
        }
    }

    private Expression parseValueToken(Tokenizer.Token token) throws UnexpectedTokenException, InvalidTokenException {
        var list = new ArrayList<>();

        parseValueToken(token, list::add);

        if (list.size() == 1) {
            return (Expression) list.get(0);
        } else {
            leftTokenAction(list, Tokenizer.TokenType.INCREASE, asSetter((x) -> UnaryExpression.add(x, new NumberObject(1).asExpression()), false));
            leftTokenAction(list, Tokenizer.TokenType.DECREASE, asSetter((x) -> UnaryExpression.remove(x, new NumberObject(1).asExpression()), false));

            mergeIntoOne(list, Tokenizer.TokenType.AND, UnaryExpression::and);
            mergeIntoOne(list, Tokenizer.TokenType.OR, UnaryExpression::or);

            mergeIntoOne(list, Tokenizer.TokenType.POWER, UnaryExpression::power);
            mergeIntoOne(list, Tokenizer.TokenType.MULTIPLY, UnaryExpression::multiply);
            mergeIntoOne(list, Tokenizer.TokenType.DIVIDE, UnaryExpression::divide);
            mergeIntoOne(list, Tokenizer.TokenType.ADD, UnaryExpression::add);
            mergeIntoOne(list, Tokenizer.TokenType.REMOVE, UnaryExpression::remove);

            mergeIntoOne(list, Tokenizer.TokenType.LESS_OR_EQUAL, UnaryExpression::lessOrEqual);
            mergeIntoOne(list, Tokenizer.TokenType.LESS_THAN, UnaryExpression::lessThan);
            mergeIntoOne(list, Tokenizer.TokenType.MORE_OR_EQUAL, UnaryExpression::moreOrEqual);
            mergeIntoOne(list, Tokenizer.TokenType.MORE_THAN, UnaryExpression::moreThan);
            mergeIntoOne(list, Tokenizer.TokenType.EQUAL, UnaryExpression::equal);
            mergeIntoOne(list, Tokenizer.TokenType.NEGATE_EQUAL, (l, r) -> new NegateExpression(UnaryExpression.equal(l, r)));
            mergeIntoOne(list, Tokenizer.TokenType.AND_DOUBLE, UnaryExpression::and);
            mergeIntoOne(list, Tokenizer.TokenType.OR_DOUBLE, UnaryExpression::or);

            mergeIntoOne(list, Tokenizer.TokenType.POWER_SET, asSetter(UnaryExpression::power));
            mergeIntoOne(list, Tokenizer.TokenType.MULTIPLY_SET, asSetter(UnaryExpression::multiply));
            mergeIntoOne(list, Tokenizer.TokenType.DIVIDE_SET, asSetter(UnaryExpression::divide));
            mergeIntoOne(list, Tokenizer.TokenType.ADD_SET, asSetter(UnaryExpression::add));
            mergeIntoOne(list, Tokenizer.TokenType.REMOVE_SET, asSetter(UnaryExpression::remove));
            mergeIntoOne(list, Tokenizer.TokenType.SET, asSetter((l, r) -> r));

            if (list.size() == 1) {
                return (Expression) list.get(0);
            } else {
                for (var obj : list) {
                    if (obj instanceof Tokenizer.Token token1) {
                        throw new UnexpectedTokenException(token1, Tokenizer.TokenType.ADD);
                    }
                }
                throw new UnexpectedTokenException(token, Tokenizer.TokenType.ADD);
            }
        }
    }

    private BiFunction<Expression, Expression, Expression> asSetter(BiFunction<Expression, Expression, Expression> base) {
        return (l, r) -> {
            if (l instanceof SettableExpression settableExpression) {
                return settableExpression.asSetter(base.apply(l, r));
            } else {
                return null;
            }
        };
    }

    private Function<Expression, Expression> asSetter(Function<Expression, Expression> base, boolean returnOld) {
        return (l) -> {
            if (l instanceof SettableExpression settableExpression) {
                if (returnOld) {
                    return settableExpression.asSetter(base.apply(l));
                } else {
                    return settableExpression.asSetterWithOldReturn(base.apply(l));
                }
            } else {
                return null;
            }
        };
    }

    private void mergeIntoOne(ArrayList<Object> list, Tokenizer.TokenType token, BiFunction<Expression, Expression, Expression> expressionBuilder) throws UnexpectedTokenException {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Tokenizer.Token token1 && token1.type() == token) {
                var left = (Expression) list.remove(i - 1);
                list.remove(i - 1);
                var right = (Expression) list.remove(i - 1);

                var val =  expressionBuilder.apply(left, right);

                if (val == null) {
                    throw new UnexpectedTokenException(token1, token);
                }

                list.add(i - 1, val);
                i--;
            }
        }
    }

    private void leftTokenAction(ArrayList<Object> list, Tokenizer.TokenType token, Function<Expression, Expression> expressionBuilder) throws UnexpectedTokenException {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) instanceof Tokenizer.Token token1 && token1.type() == token && list.get(i + 1) instanceof Expression) {
                list.remove(i);
                var right = (Expression) list.remove(i);

                var val =  expressionBuilder.apply(right);

                if (val == null) {
                    throw new UnexpectedTokenException(token1, token);
                }

                list.add(i, val);
            }
        }
    }

    private void rightTokenAction(ArrayList<Object> list, Tokenizer.TokenType token, Function<Expression, Expression> expressionBuilder) throws UnexpectedTokenException {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) instanceof Tokenizer.Token token1 && token1.type() == token && list.get(i - 1) instanceof Expression) {
                var right = (Expression) list.remove(i - 1);
                list.remove(i - 1);
                var val =  expressionBuilder.apply(right);

                if (val == null) {
                    throw new UnexpectedTokenException(token1, token);
                }

                list.add(i - 1, val);
                i--;
            }
        }
    }

    private void parseValueToken(Tokenizer.Token token, Consumer<Object> unorderedOperations) throws UnexpectedTokenException, InvalidTokenException {
        if (token.type() == Tokenizer.TokenType.INCREASE || token.type() == Tokenizer.TokenType.DECREASE || token.type() == Tokenizer.TokenType.REMOVE) {
            unorderedOperations.accept(token);
            token = this.matcher.peek();
        }

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

                case ADD, REMOVE, MULTIPLY, DIVIDE, POWER, LESS_THAN, LESS_OR_EQUAL, MORE_THAN, MORE_OR_EQUAL, EQUAL, NEGATE_EQUAL, AND, OR, AND_DOUBLE, OR_DOUBLE,
                        ADD_SET, REMOVE_SET, MULTIPLY_SET, DIVIDE_SET, POWER_SET, SET -> {
                    unorderedOperations.accept(expression);
                    unorderedOperations.accept(next);
                    parseValueToken(this.matcher.peek(), unorderedOperations);
                    return;
                }

                case INCREASE -> {
                    expression = expression instanceof SettableExpression settableExpression
                            ? settableExpression.asSetterWithOldReturn(UnaryExpression.add(expression, new NumberObject(1).asExpression()))
                            : UnaryExpression.add(expression, new NumberObject(1).asExpression());
                }

                case DECREASE -> {
                    expression = expression instanceof SettableExpression settableExpression
                            ? settableExpression.asSetterWithOldReturn(UnaryExpression.remove(expression, new NumberObject(1).asExpression()))
                            : UnaryExpression.remove(expression, new NumberObject(1).asExpression());
                }

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
                    this.matcher.back();
                    unorderedOperations.accept(expression);
                    return;
                }
            }
        }
    }

    private void parseVariableDefinition(Consumer<Expression> consumer) throws UnexpectedTokenException, InvalidTokenException {
        var id = this.matcher.peek();
        var value = XObject.NULL.asExpression();
        if (id.type() == Tokenizer.TokenType.IDENTIFIER) {
            while (true) {
                var next = this.matcher.peek();
                if (next.type() == Tokenizer.TokenType.END) {
                    this.matcher.back();
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
