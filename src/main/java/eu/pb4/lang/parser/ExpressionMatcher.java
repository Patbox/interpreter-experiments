package eu.pb4.lang.parser;

import eu.pb4.lang.exception.InvalidTokenException;
import eu.pb4.lang.exception.UnexpectedTokenException;
import eu.pb4.lang.expression.*;
import eu.pb4.lang.object.*;
import eu.pb4.lang.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExpressionMatcher {

    private final TokenReader matcher;

    public ExpressionMatcher(TokenReader matcher) {
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
            case CLASS -> consumer.accept(parseClass(token));
            default -> consumer.accept(parseExpression(token));
        }
    }

    private Expression parseClass(Tokenizer.Token token) throws UnexpectedTokenException, InvalidTokenException {
        var id = this.matcher.peek();

        if (id.type() != Tokenizer.TokenType.IDENTIFIER) {
            throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
        }

        var next = this.matcher.peek();

        String superClass = null;

        if (next.type() == Tokenizer.TokenType.EXTENDS) {
            var superToken = this.matcher.peek();

            if (superToken.type() != Tokenizer.TokenType.IDENTIFIER) {
                throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
            }
            superClass = (String) superToken.value();
            next = this.matcher.peek();
        }

        if (next.type() != Tokenizer.TokenType.SCOPE_START) {
            throw new UnexpectedTokenException(id, Tokenizer.TokenType.SCOPE_START);
        }

        FunctionExpression constructor = null;
        var fieldConstructor = new ArrayList<Pair<Pair<String, Boolean>, Expression>>();
        var staticConstructor = new ArrayList<Pair<Pair<String, Boolean>, Expression>>();

        while (!this.matcher.isDone()) {
            next = this.matcher.peek();

            if (next.type() == Tokenizer.TokenType.SCOPE_END) {
                return new DefineVariableExpression((String) id.value(), new CreateClassExpression((String) id.value(), superClass, constructor, fieldConstructor, staticConstructor, Expression.Position.from(token)), Expression.Position.betweenIn(id, next));
            }

            boolean isFinal = false;

            var fields = fieldConstructor;

            if (next.type() == Tokenizer.TokenType.CONSTRUCTOR) {
                constructor = readClassFunction();
                continue;
            }

            if (next.type() == Tokenizer.TokenType.STATIC) {
                fields = staticConstructor;
                next = this.matcher.peek();
            }

            if (next.type() == Tokenizer.TokenType.FINAL) {
                isFinal = true;
                next = this.matcher.peek();
            }

            if (next.type() == Tokenizer.TokenType.IDENTIFIER) {
                var fieldId = (String) next.value();
                Expression expression = Expression.NOOP;
                next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    this.matcher.back();
                    expression = readClassFunction();
                } else if (next.type() == Tokenizer.TokenType.SET) {
                    expression = this.parseExpression();
                }
                fields.add(new Pair<>(new Pair<>(fieldId, isFinal), expression));
            } else if (next.type() == Tokenizer.TokenType.END) {
                continue;
            } else {
                this.matcher.back();
            }
        }

        throw new UnexpectedTokenException(id, Tokenizer.TokenType.SCOPE_START);
    }
    public FunctionExpression readClassFunction() throws UnexpectedTokenException, InvalidTokenException {
        var token = this.matcher.peek();
        if (token.type() == Tokenizer.TokenType.BRACKET_START) {
            var args = new ArrayList<String>();

            while (!this.matcher.isDone()) {
                var next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.IDENTIFIER) {
                    args.add((String) next.value());

                    next = this.matcher.peek();

                    if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                        return new FunctionExpression(args, parseScoped(), Expression.Position.betweenIn(token, next));
                    } else if (next.type() == Tokenizer.TokenType.COMMA) {
                        if (this.matcher.peek().type() == Tokenizer.TokenType.IDENTIFIER) {
                            this.matcher.back();
                            continue;
                        } else {
                            throw new UnexpectedTokenException(token, Tokenizer.TokenType.IDENTIFIER);
                        }
                    } else {
                        throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                    }
                } else if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                    return new FunctionExpression(args, parseScoped(), Expression.Position.betweenIn(token, next));
                } else {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
            }
        } else {
            throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_START);
        }
        throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
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
                            return new FunctionExpression(args, parseScoped(), Expression.Position.betweenIn(token, next));
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
                        return new FunctionExpression(args, parseScoped(), Expression.Position.betweenIn(token, next));
                    }
                } else {
                    break;
                }
            }

            this.matcher.index(index);
        }

        return switch (token.type()) {
            case IDENTIFIER, STRING, NUMBER, TRUE, FALSE, NULL, BRACKET_START, SQR_BRACKET_START, SCOPE_START, INCREASE, DECREASE, TYPEOF -> parseValueToken(token);
            case NEGATE, REMOVE -> new NegateExpression(parseExpression(), Expression.Position.from(token));

            case ASYNC -> new AsyncExpression(parseScoped(), Expression.Position.from(token));

            case EXPORT -> {
                var start = this.matcher.index();
                var id = this.matcher.peek();

                if (id.type() == Tokenizer.TokenType.IDENTIFIER) {
                    yield new ExportExpression(new StringObject((String) id.value()).asExpression(Expression.Position.from(id)), parseExpression(), Expression.Position.from(token));
                } else if (id.type() == Tokenizer.TokenType.DEFAULT) {
                    yield new ExportExpression(new StringObject("").asExpression(Expression.Position.from(id)), parseExpression(), Expression.Position.from(token));
                } else if (id.type() == Tokenizer.TokenType.BRACKET_START) {
                    this.matcher.index(start);
                    yield parseValueToken(token);
                } else {
                    throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                }
            }

            case IMPORT -> {
                var start = this.matcher.index();
                var id = this.matcher.peek();
                var path = this.matcher.peek();

                if (id.type() == Tokenizer.TokenType.IDENTIFIER && path.type() == Tokenizer.TokenType.STRING) {
                    yield new DefineVariableExpression((String) id.value(), new ImportExpression(new StringObject((String) path.value()).asExpression(Expression.Position.from(id)), Expression.Position.from(token)), Expression.Position.from(token));
                } else if (id.type() == Tokenizer.TokenType.BRACKET_START) {
                    this.matcher.index(start);
                    yield parseValueToken(token);
                } else {
                    throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                }
            }
            case RETURN -> new ReturnExpression(parseEmptyExpression(), ForceReturnObject.Type.FULL, Expression.Position.from(token));
            case YIELD -> new ReturnExpression(parseEmptyExpression(), ForceReturnObject.Type.SWITCH, Expression.Position.from(token));
            case CONTINUE -> new LoopSkipExpression(false, Expression.Position.from(token));
            case BREAK -> new LoopSkipExpression(true, Expression.Position.from(token));
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

                yield new LoopWhileExpression(expression, scope, Expression.Position.from(token));
            }

            case IF -> parseIf();

            case SWITCH -> {
                var next = this.matcher.peek();
                Expression value;
                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    value = parseExpression();

                    if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                } else {
                    value = parseExpression(next);
                }

                var peek = this.matcher.peek();

                if (peek.type() != Tokenizer.TokenType.SCOPE_START) {
                    throw new UnexpectedTokenException(peek, Tokenizer.TokenType.SCOPE_START);
                }

                List<Expression> defaultExpr = List.of(Expression.NOOP);
                List<Pair<Expression, List<Expression>>> expressions = new ArrayList<>();

                while (!this.matcher.isDone()) {
                    peek = this.matcher.peek();

                    if (peek.type() == Tokenizer.TokenType.SCOPE_END) {
                        yield new SwitchExpression(value, expressions, defaultExpr, Expression.Position.from(next));
                    } else if (peek.type() == Tokenizer.TokenType.CASE) {
                        var expr = this.parseExpression();

                        if (this.matcher.peek().type() != Tokenizer.TokenType.FUNCTION_ARROW) {
                            throw new UnexpectedTokenException(peek, Tokenizer.TokenType.FUNCTION_ARROW);
                        }

                        expressions.add(new Pair<>(expr, parseScoped()));
                    } else if (peek.type() == Tokenizer.TokenType.DEFAULT) {
                        if (this.matcher.peek().type() != Tokenizer.TokenType.FUNCTION_ARROW) {
                            throw new UnexpectedTokenException(peek, Tokenizer.TokenType.FUNCTION_ARROW);
                        }

                        defaultExpr = parseScoped();
                    } else if (peek.type() == Tokenizer.TokenType.END) {
                        continue;
                    } else {
                        throw new UnexpectedTokenException(peek, Tokenizer.TokenType.SCOPE_END);
                    }
                }

                throw new UnexpectedTokenException(peek, Tokenizer.TokenType.SCOPE_END);
            }

            case FOR -> {
                var next = this.matcher.peek();
                List<Expression> init = new ArrayList<>();
                Expression expression;
                Expression post;
                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    {
                        var start = this.matcher.index();

                        var varToken = this.matcher.peek();
                        var idToken = this.matcher.peek();
                        var colonToken = this.matcher.peek();
                        if (varToken.type() == Tokenizer.TokenType.DECLARE_VAR
                                && idToken.type() == Tokenizer.TokenType.IDENTIFIER
                                && colonToken.type() == Tokenizer.TokenType.COLON
                        ) {
                            var iterator = parseExpression();
                            if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                                throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                            }
                            var scope = parseScoped();
                            yield new LoopForEachExpression((String) idToken.value(), iterator, scope, Expression.Position.from(token));
                        }

                        this.matcher.index(start);
                    }

                    parseMultiExpression(init::add);
                    init.removeIf(x -> x == Expression.NOOP);
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

                    {
                        var start = this.matcher.index();

                        var varToken = this.matcher.peek();
                        var idToken = this.matcher.peek();
                        var colonToken = this.matcher.peek();
                        if (varToken.type() == Tokenizer.TokenType.DECLARE_VAR
                                && idToken.type() == Tokenizer.TokenType.IDENTIFIER
                                && colonToken.type() == Tokenizer.TokenType.COLON
                        ) {
                            yield new LoopForEachExpression((String) idToken.value(), parseExpression(), parseScoped(), Expression.Position.from(token));
                        }

                        this.matcher.index(start);
                    }

                    parseMultiExpression(init::add);
                    init.removeIf(x -> x == Expression.NOOP);
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    expression = parseExpression();
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    post = parseExpression();

                }

                yield new LoopForExpression(init, expression, post, parseScoped(), Expression.Position.from(token));
            }
            case END -> Expression.NOOP;
            default -> throw new UnexpectedTokenException(token, Tokenizer.TokenType.END);
        };
    }

    private Expression parseIf() throws UnexpectedTokenException, InvalidTokenException {
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

        next = this.matcher.peek();
        if (next.type() == Tokenizer.TokenType.ELSE) {
            if (this.matcher.peek().type() == Tokenizer.TokenType.IF) {
                this.matcher.back();
                return new IfElseExpression(expression, scope, List.of(parseIf()), Expression.Position.from(next));
            } else {
                this.matcher.back();
                return new IfElseExpression(expression, scope, parseScoped(), Expression.Position.from(next));
            }
        } else {
            this.matcher.back();
            return new IfElseExpression(expression, scope, List.of(), Expression.Position.from(next));
        }
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
                    list.removeIf(x -> x == Expression.NOOP);

                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        this.matcher.back();
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
            mergeLeftTokenExpression(list, Tokenizer.TokenType.TYPEOF, TypeOfExpression::of);

            mergeLeftTokenExpression(list, Tokenizer.TokenType.INCREASE, asSetter((x) -> UnaryExpression.add(x, new NumberObject(1).asExpression(Expression.Position.EMPTY)), false));
            mergeLeftTokenExpression(list, Tokenizer.TokenType.DECREASE, asSetter((x) -> UnaryExpression.remove(x, new NumberObject(1).asExpression(Expression.Position.EMPTY)), false));

            mergeUnaryExpressions(list, Tokenizer.TokenType.AND, UnaryExpression::and);
            mergeUnaryExpressions(list, Tokenizer.TokenType.OR, UnaryExpression::or);
            mergeUnaryExpressions(list, Tokenizer.TokenType.SHIFT_LEFT, UnaryExpression::shiftLeft);
            mergeUnaryExpressions(list, Tokenizer.TokenType.SHIFT_RIGHT, UnaryExpression::shiftRight);

            mergeUnaryExpressions(list, Tokenizer.TokenType.POWER, UnaryExpression::power);
            mergeUnaryExpressions(list, Tokenizer.TokenType.MULTIPLY, UnaryExpression::multiply);
            mergeUnaryExpressions(list, Tokenizer.TokenType.DIVIDE, UnaryExpression::divide);
            mergeUnaryExpressions(list, Tokenizer.TokenType.DIVIDE_REST, UnaryExpression::divideRest);
            mergeUnaryExpressions(list, Tokenizer.TokenType.ADD, UnaryExpression::add);
            mergeUnaryExpressions(list, Tokenizer.TokenType.REMOVE, UnaryExpression::remove);

            mergeUnaryExpressions(list, Tokenizer.TokenType.LESS_OR_EQUAL, UnaryExpression::lessOrEqual);
            mergeUnaryExpressions(list, Tokenizer.TokenType.LESS_THAN, UnaryExpression::lessThan);
            mergeUnaryExpressions(list, Tokenizer.TokenType.MORE_OR_EQUAL, UnaryExpression::moreOrEqual);
            mergeUnaryExpressions(list, Tokenizer.TokenType.MORE_THAN, UnaryExpression::moreThan);
            mergeUnaryExpressions(list, Tokenizer.TokenType.EQUAL, UnaryExpression::equal);
            mergeUnaryExpressions(list, Tokenizer.TokenType.NEGATE_EQUAL, (l, r) -> new NegateExpression(UnaryExpression.equal(l, r), Expression.Position.betweenEx(l.info(), r.info())));
            mergeUnaryExpressions(list, Tokenizer.TokenType.AND_DOUBLE, UnaryExpression::and);
            mergeUnaryExpressions(list, Tokenizer.TokenType.OR_DOUBLE, UnaryExpression::or);

            mergeUnaryExpressions(list, Tokenizer.TokenType.POWER_SET, asSetter(UnaryExpression::power));
            mergeUnaryExpressions(list, Tokenizer.TokenType.MULTIPLY_SET, asSetter(UnaryExpression::multiply));
            mergeUnaryExpressions(list, Tokenizer.TokenType.DIVIDE_SET, asSetter(UnaryExpression::divide));
            mergeUnaryExpressions(list, Tokenizer.TokenType.ADD_SET, asSetter(UnaryExpression::add));
            mergeUnaryExpressions(list, Tokenizer.TokenType.REMOVE_SET, asSetter(UnaryExpression::remove));
            mergeUnaryExpressions(list, Tokenizer.TokenType.SET, asSetter((l, r) -> r));

            mergeTrinaryExpressions(list, Tokenizer.TokenType.QUESTION_MARK, Tokenizer.TokenType.COLON, IfElseExpression::trinary);

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

    private void mergeUnaryExpressions(ArrayList<Object> list, Tokenizer.TokenType token, BiFunction<Expression, Expression, Expression> expressionBuilder) throws UnexpectedTokenException {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Tokenizer.Token token1 && token1.type() == token) {
                var left = (Expression) list.remove(i - 1);
                list.remove(i - 1);
                var right = (Expression) list.remove(i - 1);

                var val = expressionBuilder.apply(left, right);

                if (val == null) {
                    throw new UnexpectedTokenException(token1, token);
                }

                list.add(i - 1, val);
                i--;
            }
        }
    }

    private void mergeTrinaryExpressions(ArrayList<Object> list, Tokenizer.TokenType tokenLeft, Tokenizer.TokenType tokenRight, TriFunction<Expression, Expression, Expression, Expression> expressionBuilder) throws UnexpectedTokenException {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Tokenizer.Token token1 && token1.type() == tokenLeft && list.get(i + 2) instanceof Tokenizer.Token token2 && token2.type() == tokenRight) {
                var left = (Expression) list.remove(i - 1);
                list.remove(i - 1);
                var middle = (Expression) list.remove(i - 1);
                list.remove(i - 1);
                var right = (Expression) list.remove(i - 1);


                var val = expressionBuilder.apply(left, middle, right);

                if (val == null) {
                    throw new UnexpectedTokenException(token1, tokenLeft);
                }

                list.add(i - 1, val);
                i--;
            }
        }
    }

    private void mergeLeftTokenExpression(ArrayList<Object> list, Tokenizer.TokenType token, Function<Expression, Expression> expressionBuilder) throws UnexpectedTokenException {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) instanceof Tokenizer.Token token1 && token1.type() == token && list.get(i + 1) instanceof Expression) {
                list.remove(i);
                var right = (Expression) list.remove(i);

                var val = expressionBuilder.apply(right);

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
                var val = expressionBuilder.apply(right);

                if (val == null) {
                    throw new UnexpectedTokenException(token1, token);
                }

                list.add(i - 1, val);
                i--;
            }
        }
    }

    private void parseValueToken(Tokenizer.Token token, Consumer<Object> unorderedOperations) throws UnexpectedTokenException, InvalidTokenException {
        if (token.type() == Tokenizer.TokenType.INCREASE || token.type() == Tokenizer.TokenType.DECREASE || token.type() == Tokenizer.TokenType.REMOVE
                || token.type() == Tokenizer.TokenType.TYPEOF) {
            unorderedOperations.accept(token);
            token = this.matcher.peek();
        }

        Expression expression = switch (token.type()) {
            case IDENTIFIER -> new GetVariableExpression((String) token.value(), Expression.Position.from(token));
            case BRACKET_START -> {
                var exp = parseExpression();
                var next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                    yield exp;
                } else {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
            }
            case SQR_BRACKET_START -> {
                Tokenizer.Token next;

                var val = new ArrayList<Expression>();

                while ((next = this.matcher.peek()).type() != Tokenizer.TokenType.SQR_BRACKET_END && !this.matcher.isDone()) {
                    this.matcher.back();
                    val.add(parseExpression());
                    next = this.matcher.peek();
                    if (next.type() == Tokenizer.TokenType.SQR_BRACKET_END) {
                        yield new ListExpression(val.toArray(new Expression[0]), Expression.Position.betweenIn(token, next));
                    } else if (next.type() != Tokenizer.TokenType.COMMA) {
                        throw new UnexpectedTokenException(next, Tokenizer.TokenType.COMMA);
                    }
                }

                if (next.type() == Tokenizer.TokenType.SQR_BRACKET_END) {
                    yield new DirectObjectExpression(new ListObject(), Expression.Position.betweenIn(token, next));
                } else {
                    throw new UnexpectedTokenException(next, Tokenizer.TokenType.SQR_BRACKET_END);
                }
            }

            case SCOPE_START -> {
                Tokenizer.Token next;
                var val = new ArrayList<Expression[]>();
                boolean isMap = false;
                if (this.matcher.peek().type() == Tokenizer.TokenType.SCOPE_START) {
                    isMap = true;
                } else {
                    this.matcher.back();
                }

                while ((next = this.matcher.peek()).type() != Tokenizer.TokenType.SCOPE_END && !this.matcher.isDone()) {
                    this.matcher.back();
                    var pair = new Expression[2];
                    var possibleId = this.matcher.peek();

                    if (possibleId.value() instanceof String str && !isMap) {
                        pair[0] = new DirectObjectExpression(new StringObject(str), Expression.Position.from(possibleId));
                    } else {
                        this.matcher.back();
                        pair[0] = parseExpression();
                    }

                    if (this.matcher.peek().type() != Tokenizer.TokenType.COLON) {
                        throw new UnexpectedTokenException(next, Tokenizer.TokenType.COLON);
                    }

                    pair[1] = parseExpression();

                    val.add(pair);

                    next = this.matcher.peek();
                    if (next.type() == Tokenizer.TokenType.SCOPE_END) {
                        if (isMap) {
                            if ((next = this.matcher.peek()).type() == Tokenizer.TokenType.SCOPE_END) {
                                yield new MapExpression(val.toArray(new Expression[0][]), Expression.Position.betweenIn(token, next));
                            } else {
                                throw new UnexpectedTokenException(next, Tokenizer.TokenType.SCOPE_END);
                            }
                        } else {
                            yield new StringMapExpression(val.toArray(new Expression[0][]), Expression.Position.betweenIn(token, next));
                        }
                    } else if (next.type() != Tokenizer.TokenType.COMMA) {
                        throw new UnexpectedTokenException(next, Tokenizer.TokenType.COMMA);
                    }
                }

                if (next.type() == Tokenizer.TokenType.SCOPE_END) {
                    if (isMap) {
                        if ((next = this.matcher.peek()).type() == Tokenizer.TokenType.SCOPE_END) {
                            yield new DirectObjectExpression(new MapObject(), Expression.Position.betweenIn(token, next));
                        } else {
                            throw new UnexpectedTokenException(next, Tokenizer.TokenType.SCOPE_END);
                        }
                    } else {
                        yield new DirectObjectExpression(new StringMapObject(), Expression.Position.betweenIn(token, next));
                    }
                } else {
                    throw new UnexpectedTokenException(next, Tokenizer.TokenType.SCOPE_END);
                }

            }

            case IMPORT -> {
                this.matcher.peek();
                var x = new ImportExpression(parseExpression(), Expression.Position.from(token));
                if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
                yield x;
            }
            case EXPORT -> {
                this.matcher.peek();
                var arg1 = parseExpression();
                if (this.matcher.peek().type() != Tokenizer.TokenType.COMMA) {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.COMMA);
                }

                var x = new ExportExpression(arg1, parseExpression(), Expression.Position.from(token));
                if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
                yield x;
            }
            default -> DirectObjectExpression.fromToken(token);
        };

        while (!this.matcher.isDone()) {
            var next = this.matcher.peek();

            switch (next.type()) {
                case DOT -> {
                    var key = this.matcher.peek();
                    if (key.type() == Tokenizer.TokenType.IDENTIFIER) {
                        expression = new GetStringExpression(expression, (String) key.value(), Expression.Position.from(token));
                    }
                }

                case ADD, REMOVE, MULTIPLY, DIVIDE, POWER, LESS_THAN, LESS_OR_EQUAL, MORE_THAN, MORE_OR_EQUAL, EQUAL, NEGATE_EQUAL, AND, OR, AND_DOUBLE, OR_DOUBLE,
                        ADD_SET, REMOVE_SET, MULTIPLY_SET, DIVIDE_SET, POWER_SET, SET, SHIFT_LEFT, SHIFT_RIGHT, DIVIDE_REST -> {
                    unorderedOperations.accept(expression);
                    unorderedOperations.accept(next);
                    parseValueToken(this.matcher.peek(), unorderedOperations);
                    return;
                }

                case INCREASE -> {
                    expression = expression instanceof SettableExpression settableExpression
                            ? settableExpression.asSetterWithOldReturn(UnaryExpression.add(expression, new NumberObject(1).asExpression(Expression.Position.EMPTY)))
                            : UnaryExpression.add(expression, new NumberObject(1).asExpression(Expression.Position.EMPTY));
                }

                case DECREASE -> {
                    expression = expression instanceof SettableExpression settableExpression
                            ? settableExpression.asSetterWithOldReturn(UnaryExpression.remove(expression, new NumberObject(1).asExpression(Expression.Position.EMPTY)))
                            : UnaryExpression.remove(expression, new NumberObject(1).asExpression(Expression.Position.EMPTY));
                }

                case SQR_BRACKET_START -> {
                    expression = new GetObjectExpression(expression, parseExpression(this.matcher.peek()), Expression.Position.from(token));

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

                                expression = new CallFunctionException(expression, args.toArray(new Expression[0]), Expression.Position.from(token));
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
        var value = XObject.NULL.asExpression(Expression.Position.EMPTY);
        if (id.type() == Tokenizer.TokenType.IDENTIFIER) {
            while (true) {
                var next = this.matcher.peek();
                if (next.type() == Tokenizer.TokenType.SET) {
                    value = parseExpression();
                } else if (next.type() == Tokenizer.TokenType.COMMA) {
                    consumer.accept(new DefineVariableExpression((String) id.value(), value, Expression.Position.betweenIn(id, next)));
                    id = this.matcher.peek();
                    if (id.type() != Tokenizer.TokenType.IDENTIFIER) {
                        throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                    }

                    value = XObject.NULL.asExpression(Expression.Position.EMPTY);
                } else {
                    this.matcher.back();
                    consumer.accept(new DefineVariableExpression((String) id.value(), value, Expression.Position.betweenIn(id, next)));
                    break;
                }
            }
        } else {
            throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
        }
    }

    private interface TriFunction<T, T1, T2, T3> {
        T3 apply(T t, T1 t1, T2 t2);
    }
}
