package eu.pb4.lang.parser;

import eu.pb4.lang.exception.InvalidTokenException;
import eu.pb4.lang.exception.UnexpectedTokenException;
import eu.pb4.lang.expression.*;
import eu.pb4.lang.object.*;
import eu.pb4.lang.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExpressionMatcher {

    private final TokenReader matcher;
    private final String script;

    public ExpressionMatcher(TokenReader matcher, String script) {
        this.script = script;
        this.matcher = matcher;
    }


    public List<Expression> build() throws UnexpectedTokenException, InvalidTokenException {
        var list = new ArrayList<Expression>();

        var scope = new VariableId2IntMapper(null, 0);
        scope.declare("this");
        scope.declare("super");

        while (!this.matcher.isDone()) {
            parseMultiExpression(list::add, scope);
        }
        list.removeIf(x -> x == Expression.NOOP);

        return list;
    }


    public void parseMultiExpression(Consumer<Expression> consumer, VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        var token = this.matcher.peek();

        var isFinal = false;
        if (token.type() == Tokenizer.TokenType.FINAL) {
            isFinal = true;
            token = this.matcher.peek();
        }

        switch (token.type()) {
            case DECLARE_VAR -> parseVariableDefinition(consumer, isFinal, scope);
            case CLASS -> {
                var classExpr = parseClass(token, isFinal, scope);
                consumer.accept(new DefineVariableExpression(classExpr.name(), scope.declare(classExpr.name()), classExpr, true, classExpr.info()));
            }
            default -> consumer.accept(parseExpression(token, scope));
        }
    }

    private CreateClassExpression parseClass(Tokenizer.Token token, boolean isFinalClass, VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
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

        scope.declare((String) id.value());

        FunctionExpression constructor = null;
        var fieldConstructor = new ArrayList<Pair<Pair<String, Boolean>, Expression>>();
        var staticConstructor = new ArrayList<Pair<Pair<String, Boolean>, Expression>>();

        while (!this.matcher.isDone()) {
            next = this.matcher.peek();

            if (next.type() == Tokenizer.TokenType.SCOPE_END) {
                return new CreateClassExpression((String) id.value(), superClass, superClass != null ? scope.get(superClass) : -1, constructor, fieldConstructor, staticConstructor, isFinalClass,
                                Expression.Position.from(token, script));
            }

            boolean isFinal = false;

            var fields = fieldConstructor;

            if (next.type() == Tokenizer.TokenType.CONSTRUCTOR) {
                constructor = readClassFunction(scope);
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
                    expression = readClassFunction(scope);
                } else if (next.type() == Tokenizer.TokenType.SET) {
                    expression = this.parseExpression(scope);
                }
                fields.add(new Pair<>(new Pair<>(fieldId, isFinal), expression));
            } else if (next.type() == Tokenizer.TokenType.CLASS) {
                var classExpr = parseClass(next, isFinal, scope);

                fields.add(new Pair<>(new Pair<>(classExpr.name(), isFinal), classExpr));
            } else if (next.type() == Tokenizer.TokenType.END) {
                continue;
            } else {
                throw new UnexpectedTokenException(id, Tokenizer.TokenType.SCOPE_START);
            }
        }

        throw new UnexpectedTokenException(id, Tokenizer.TokenType.SCOPE_START);
    }
    public FunctionExpression readClassFunction(VariableId2IntMapper variableScope) throws UnexpectedTokenException, InvalidTokenException {
        var token = this.matcher.peek();
        if (token.type() == Tokenizer.TokenType.BRACKET_START) {
            var lScope = variableScope.up();
            var args = new ArrayList<String>();
            var argsIds = new IntArrayList();

            while (!this.matcher.isDone()) {
                var next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.IDENTIFIER) {
                    var nextId = (String) next.value();
                    args.add(nextId);
                    argsIds.add(lScope.declare(nextId));

                    next = this.matcher.peek();

                    if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                        return new FunctionExpression(args, argsIds.toIntArray(), parseScoped(lScope), Expression.Position.betweenIn(token, next, script));
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
                    return new FunctionExpression(args, argsIds.toIntArray(), parseScoped(lScope), Expression.Position.betweenIn(token, next, script));
                } else {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
            }
        } else {
            throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_START);
        }
        throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
    }


    public Expression parseExpression(VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        return parseExpression(this.matcher.peek(), scope);
    }

    public Expression parseExpression(Tokenizer.Token token, VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        if (token.type() == Tokenizer.TokenType.BRACKET_START) {
            var index = this.matcher.index();
            var lScope = scope.up();
            var args = new ArrayList<String>();
            var argsIds = new IntArrayList();

            while (!this.matcher.isDone()) {
                var next = this.matcher.peek();

                if (next.type() == Tokenizer.TokenType.IDENTIFIER) {
                    var argId = (String) next.value();
                    args.add(argId);
                    argsIds.add(lScope.declare(argId));

                    next = this.matcher.peek();

                    if (next.type() == Tokenizer.TokenType.BRACKET_END) {
                        if (this.matcher.peek().type() == Tokenizer.TokenType.FUNCTION_ARROW) {
                            return new FunctionExpression(args, argsIds.toIntArray(), parseScoped(lScope), Expression.Position.betweenIn(token, next, script));
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
                        return new FunctionExpression(args,  argsIds.toIntArray(), parseScoped(lScope), Expression.Position.betweenIn(token, next, script));
                    }
                } else {
                    break;
                }
            }

            this.matcher.index(index);
        }

        return switch (token.type()) {
            case IDENTIFIER, STRING, NUMBER, TRUE, FALSE, NULL, BRACKET_START, SQR_BRACKET_START, SCOPE_START, INCREASE, DECREASE, TYPEOF -> parseValueToken(token, scope);
            case NEGATE, SUBTRACT -> new NegateExpression(parseExpression(scope), Expression.Position.from(token, script));

            case ASYNC -> new AsyncExpression(parseScoped(scope), Expression.Position.from(token, script));

            case EXPORT -> {
                var start = this.matcher.index();
                var id = this.matcher.peek();

                if (id.type() == Tokenizer.TokenType.IDENTIFIER) {
                    yield new ExportExpression(new StringObject((String) id.value()).asExpression(Expression.Position.from(id, script)), parseExpression(scope), Expression.Position.from(token, script));
                } else if (id.type() == Tokenizer.TokenType.DEFAULT) {
                    yield new ExportExpression(new StringObject("").asExpression(Expression.Position.from(id, script)), parseExpression(scope), Expression.Position.from(token, script));
                } else if (id.type() == Tokenizer.TokenType.BRACKET_START) {
                    this.matcher.index(start);
                    yield parseValueToken(token, scope);
                } else {
                    throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                }
            }

            case IMPORT -> {
                var start = this.matcher.index();
                var id = this.matcher.peek();
                var path = this.matcher.peek();

                if (id.type() == Tokenizer.TokenType.IDENTIFIER && path.type() == Tokenizer.TokenType.STRING) {
                    yield new DefineVariableExpression((String) id.value(), scope.declare((String) id.value()), new ImportExpression(new StringObject((String) path.value()).asExpression(Expression.Position.from(id, script)), Expression.Position.from(token, script)), true, Expression.Position.from(token, script));
                } else if (id.type() == Tokenizer.TokenType.BRACKET_START) {
                    this.matcher.index(start);
                    yield parseValueToken(token, scope);
                } else {
                    throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                }
            }
            case RETURN -> new ReturnExpression(parseEmptyExpression(scope), ForceReturnObject.Type.FULL, Expression.Position.from(token, script));
            case YIELD -> new ReturnExpression(parseEmptyExpression(scope), ForceReturnObject.Type.SWITCH, Expression.Position.from(token, script));
            case CONTINUE -> new LoopSkipExpression(false, Expression.Position.from(token, script));
            case BREAK -> new LoopSkipExpression(true, Expression.Position.from(token, script));
            case WHILE -> {
                var next = this.matcher.peek();
                Expression expression;
                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    expression = parseExpression(token, scope);

                    if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                } else {
                    expression = parseExpression(next, scope);
                }

                yield new LoopWhileExpression(expression, parseScoped(scope), Expression.Position.from(token, script));
            }

            case DO -> {
                var scopeExpr = parseScoped(scope);

                var next = this.matcher.peek();

                if (next.type() != Tokenizer.TokenType.WHILE) {
                    throw new UnexpectedTokenException(next, Tokenizer.TokenType.WHILE);
                }

                next = this.matcher.peek();
                Expression expression;
                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    expression = parseExpression(scope);

                    if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                } else {
                    expression = parseExpression(next, scope);
                }

                yield new LoopDoWhileExpression(expression, scopeExpr, Expression.Position.from(token, script));
            }

            case IF -> parseIf(scope);

            case SWITCH -> {
                var next = this.matcher.peek();
                Expression value;
                if (next.type() == Tokenizer.TokenType.BRACKET_START) {
                    value = parseExpression(scope);

                    if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                    }
                } else {
                    value = parseExpression(next, scope);
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
                        yield new SwitchExpression(value, expressions, defaultExpr, Expression.Position.from(next, script));
                    } else if (peek.type() == Tokenizer.TokenType.CASE) {
                        var expr = this.parseExpression(scope);

                        if (this.matcher.peek().type() != Tokenizer.TokenType.FUNCTION_ARROW) {
                            throw new UnexpectedTokenException(peek, Tokenizer.TokenType.FUNCTION_ARROW);
                        }

                        expressions.add(new Pair<>(expr, parseScoped(scope)));
                    } else if (peek.type() == Tokenizer.TokenType.DEFAULT) {
                        if (this.matcher.peek().type() != Tokenizer.TokenType.FUNCTION_ARROW) {
                            throw new UnexpectedTokenException(peek, Tokenizer.TokenType.FUNCTION_ARROW);
                        }

                        defaultExpr = parseScoped(scope);
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
                var lScope = scope.up();
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
                            var iterator = parseExpression(token, lScope);
                            if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                                throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
                            }
                            yield new LoopForEachExpression((String) idToken.value(), lScope.declare((String) idToken.value()), iterator, parseScoped(lScope), Expression.Position.from(token, script));
                        }

                        this.matcher.index(start);
                    }

                    parseMultiExpression(init::add, lScope);
                    init.removeIf(x -> x == Expression.NOOP);
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    expression = parseExpression(lScope);
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }

                    post = parseExpression(lScope);
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
                            yield new LoopForEachExpression((String) idToken.value(), lScope.declare((String) idToken.value()), parseExpression(lScope), parseScoped(lScope), Expression.Position.from(token, script));
                        }

                        this.matcher.index(start);
                    }

                    parseMultiExpression(init::add, lScope);
                    init.removeIf(x -> x == Expression.NOOP);
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    expression = parseExpression(lScope);
                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.END);
                    }
                    post = parseExpression(lScope);

                }

                yield new LoopForExpression(init, expression, post, parseScoped(lScope), Expression.Position.from(token, script));
            }
            case END -> Expression.NOOP;
            default -> throw new UnexpectedTokenException(token, Tokenizer.TokenType.END);
        };
    }

    private Expression parseIf(VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        var next = this.matcher.peek();
        Expression expression;
        if (next.type() == Tokenizer.TokenType.BRACKET_START) {
            expression = parseExpression(scope);

            if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                throw new UnexpectedTokenException(this.matcher.previous(), Tokenizer.TokenType.BRACKET_END);
            }
        } else {
            expression = parseExpression(next, scope);
        }

        var scopeExr = parseScoped(scope);

        next = this.matcher.peek();
        if (next.type() == Tokenizer.TokenType.ELSE) {
            if (this.matcher.peek().type() == Tokenizer.TokenType.IF) {
                this.matcher.back();
                return new IfElseExpression(expression, scopeExr, List.of(parseIf(scope)), Expression.Position.from(next, script));
            } else {
                this.matcher.back();
                return new IfElseExpression(expression, scopeExr, parseScoped(scope), Expression.Position.from(next, script));
            }
        } else {
            this.matcher.back();
            return new IfElseExpression(expression, scopeExr, List.of(), Expression.Position.from(next, script));
        }
    }

    private Expression parseEmptyExpression(VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        var next = this.matcher.peek();

        if (next.type() == Tokenizer.TokenType.END) {
            return Expression.NOOP;
        } else {
            return this.parseExpression(next, scope);
        }
    }

    private List<Expression> parseScoped(VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        var peek = this.matcher.peek();

        if (peek.type() == Tokenizer.TokenType.SCOPE_START) {
            scope = scope.up();
            var list = new ArrayList<Expression>();

            while (!this.matcher.isDone()) {
                var token = this.matcher.peek();
                if (token.type() == Tokenizer.TokenType.SCOPE_END) {
                    return list;
                } else {
                    this.matcher.back();
                    parseMultiExpression(list::add, scope);
                    list.removeIf(x -> x == Expression.NOOP);

                    if (this.matcher.peek().type() != Tokenizer.TokenType.END) {
                        this.matcher.back();
                    }
                }
            }
            throw new UnexpectedTokenException(peek, Tokenizer.TokenType.SCOPE_END);
        } else {
            this.matcher.back();
            return List.of(parseExpression(scope));
        }
    }

    private Expression parseValueToken(Tokenizer.Token token, VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        var list = new ArrayList<>();

        parseValueToken(token, list::add, scope);

        if (list.size() == 1) {
            return (Expression) list.get(0);
        } else {
            mergeLeftTokenExpression(list, Tokenizer.TokenType.TYPEOF, TypeOfExpression::of);

            mergeLeftTokenExpression(list, Tokenizer.TokenType.INCREASE, asSetter((x) -> new UnaryExpression.Add(x, NumberObject.of(1).asExpression(Expression.Position.EMPTY)), false));
            mergeLeftTokenExpression(list, Tokenizer.TokenType.DECREASE, asSetter((x) -> new UnaryExpression.Subtract(x, NumberObject.of(1).asExpression(Expression.Position.EMPTY)), false));

            mergeUnaryExpressions(list, Tokenizer.TokenType.AND, UnaryExpression.And::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.OR, UnaryExpression.Or::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.SHIFT_LEFT, UnaryExpression.ShiftLeft::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.SHIFT_RIGHT, UnaryExpression.ShiftRight::new);

            mergeUnaryExpressions(list, Tokenizer.TokenType.POWER, UnaryExpression.Power::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.MULTIPLY, UnaryExpression.Multiply::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.DIVIDE, UnaryExpression.Divide::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.DIVIDE_REST, UnaryExpression.DivideRest::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.ADD, UnaryExpression.Add::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.SUBTRACT, UnaryExpression.Subtract::new);

            mergeUnaryExpressions(list, Tokenizer.TokenType.LESS_OR_EQUAL, UnaryExpression.LessOrEqual::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.LESS_THAN, UnaryExpression.LessThan::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.MORE_OR_EQUAL, UnaryExpression.MoreOrEqual::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.MORE_THAN, UnaryExpression.MoreThan::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.EQUAL, UnaryExpression.Equal::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.NEGATE_EQUAL, (l, r) -> new NegateExpression(new UnaryExpression.Equal(l, r), Expression.Position.betweenEx(l.info(), r.info())));
            mergeUnaryExpressions(list, Tokenizer.TokenType.AND_DOUBLE, UnaryExpression.And::new);
            mergeUnaryExpressions(list, Tokenizer.TokenType.OR_DOUBLE, UnaryExpression.Or::new);

            mergeUnaryExpressions(list, Tokenizer.TokenType.POWER_SET, asSetter(UnaryExpression.Power::new));
            mergeUnaryExpressions(list, Tokenizer.TokenType.MULTIPLY_SET, asSetter(UnaryExpression.Multiply::new));
            mergeUnaryExpressions(list, Tokenizer.TokenType.DIVIDE_SET, asSetter(UnaryExpression.Divide::new));
            mergeUnaryExpressions(list, Tokenizer.TokenType.ADD_SET, asSetter(UnaryExpression.Add::new));
            mergeUnaryExpressions(list, Tokenizer.TokenType.SUBTRACT_SET, asSetter(UnaryExpression.Subtract::new));
            mergeUnaryExpressions(list, Tokenizer.TokenType.AND_SET, asSetter(UnaryExpression.And::new));
            mergeUnaryExpressions(list, Tokenizer.TokenType.OR_SET, asSetter(UnaryExpression.Or::new));
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
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof Tokenizer.Token token1 && token1.type() == token
                        && list.get(i - 1) instanceof DirectObjectExpression dLeft && list.get(i + 1) instanceof DirectObjectExpression dRight && dLeft.object().isContextless() && dRight.object().isContextless()) {
                    var left = (Expression) list.remove(i - 1);
                    list.remove(i - 1);
                    var right = (Expression) list.remove(i - 1);

                    var val = expressionBuilder.apply(left, right);

                    if (val == null) {
                        throw new UnexpectedTokenException(token1, token);
                    }

                    changed = true;

                    try {
                        list.add(i - 1, new DirectObjectExpression(val.execute(null), val.info()));
                    } catch (Throwable e) {
                        list.add(i - 1, val);
                    }
                    i--;
                }
            }
        } while (changed);

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

    private void parseValueToken(Tokenizer.Token token, Consumer<Object> unorderedOperations, VariableId2IntMapper variableScope) throws UnexpectedTokenException, InvalidTokenException {
        if (token.type() == Tokenizer.TokenType.INCREASE || token.type() == Tokenizer.TokenType.DECREASE || token.type() == Tokenizer.TokenType.SUBTRACT
                || token.type() == Tokenizer.TokenType.TYPEOF) {
            unorderedOperations.accept(token);
            token = this.matcher.peek();
        }

        Expression expression = switch (token.type()) {
            case IDENTIFIER -> new GetVariableExpression((String) token.value(), variableScope.get((String) token.value()), Expression.Position.from(token, script));
            case BRACKET_START -> {
                var exp = parseExpression(variableScope);
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
                    val.add(parseExpression(variableScope));
                    next = this.matcher.peek();
                    if (next.type() == Tokenizer.TokenType.SQR_BRACKET_END) {
                        yield new ListExpression(val.toArray(new Expression[0]), Expression.Position.betweenIn(token, next, script));
                    } else if (next.type() != Tokenizer.TokenType.COMMA) {
                        throw new UnexpectedTokenException(next, Tokenizer.TokenType.COMMA);
                    }
                }

                if (next.type() == Tokenizer.TokenType.SQR_BRACKET_END) {
                    yield new DirectObjectExpression(new ListObject(), Expression.Position.betweenIn(token, next, script));
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
                        pair[0] = new DirectObjectExpression(new StringObject(str), Expression.Position.from(possibleId, script));
                    } else {
                        this.matcher.back();
                        pair[0] = parseExpression(variableScope);
                    }

                    if (this.matcher.peek().type() != Tokenizer.TokenType.COLON) {
                        throw new UnexpectedTokenException(next, Tokenizer.TokenType.COLON);
                    }

                    pair[1] = parseExpression(variableScope);

                    val.add(pair);

                    next = this.matcher.peek();
                    if (next.type() == Tokenizer.TokenType.SCOPE_END) {
                        if (isMap) {
                            if ((next = this.matcher.peek()).type() == Tokenizer.TokenType.SCOPE_END) {
                                yield new MapExpression(val.toArray(new Expression[0][]), Expression.Position.betweenIn(token, next, script));
                            } else {
                                throw new UnexpectedTokenException(next, Tokenizer.TokenType.SCOPE_END);
                            }
                        } else {
                            yield new StringMapExpression(val.toArray(new Expression[0][]), Expression.Position.betweenIn(token, next, script));
                        }
                    } else if (next.type() != Tokenizer.TokenType.COMMA) {
                        throw new UnexpectedTokenException(next, Tokenizer.TokenType.COMMA);
                    }
                }

                if (next.type() == Tokenizer.TokenType.SCOPE_END) {
                    if (isMap) {
                        if ((next = this.matcher.peek()).type() == Tokenizer.TokenType.SCOPE_END) {
                            yield new DirectObjectExpression(new MapObject(), Expression.Position.betweenIn(token, next, script));
                        } else {
                            throw new UnexpectedTokenException(next, Tokenizer.TokenType.SCOPE_END);
                        }
                    } else {
                        yield new DirectObjectExpression(new StringMapObject(), Expression.Position.betweenIn(token, next, script));
                    }
                } else {
                    throw new UnexpectedTokenException(next, Tokenizer.TokenType.SCOPE_END);
                }

            }

            case IMPORT -> {
                this.matcher.peek();
                var x = new ImportExpression(parseExpression(variableScope), Expression.Position.from(token, script));
                if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
                yield x;
            }
            case EXPORT -> {
                this.matcher.peek();
                var arg1 = parseExpression(variableScope);
                if (this.matcher.peek().type() != Tokenizer.TokenType.COMMA) {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.COMMA);
                }

                var x = new ExportExpression(arg1, parseExpression(variableScope), Expression.Position.from(token, script));
                if (this.matcher.peek().type() != Tokenizer.TokenType.BRACKET_END) {
                    throw new UnexpectedTokenException(token, Tokenizer.TokenType.BRACKET_END);
                }
                yield x;
            }
            default -> DirectObjectExpression.fromToken(token, this.script);
        };

        while (!this.matcher.isDone()) {
            var next = this.matcher.peek();

            switch (next.type()) {
                case DOT -> {
                    var key = this.matcher.peek();
                    if (key.type() == Tokenizer.TokenType.IDENTIFIER) {
                        expression = new GetStringExpression(expression, (String) key.value(), Expression.Position.from(token, script));
                    } else if (key.type() == Tokenizer.TokenType.CLASS) {
                        expression = new GetClassExpression(expression, Expression.Position.from(token, script));
                    }
                }

                case ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, LESS_THAN, LESS_OR_EQUAL, MORE_THAN, MORE_OR_EQUAL, EQUAL, NEGATE_EQUAL, AND, OR, AND_DOUBLE, OR_DOUBLE,
                        ADD_SET, SUBTRACT_SET, MULTIPLY_SET, DIVIDE_SET, POWER_SET, SET, SHIFT_LEFT, SHIFT_RIGHT, DIVIDE_REST, OR_SET, AND_SET -> {
                    unorderedOperations.accept(expression);
                    unorderedOperations.accept(next);
                    parseValueToken(this.matcher.peek(), unorderedOperations, variableScope);
                    return;
                }

                case INCREASE -> {
                    expression = expression instanceof SettableExpression settableExpression
                            ? settableExpression.asSetterWithOldReturn(new UnaryExpression.Add(expression, NumberObject.of(1).asExpression(Expression.Position.EMPTY)))
                            : new UnaryExpression.Add(expression, NumberObject.of(1).asExpression(Expression.Position.EMPTY));
                }

                case DECREASE -> {
                    expression = expression instanceof SettableExpression settableExpression
                            ? settableExpression.asSetterWithOldReturn(new UnaryExpression.Subtract(expression, NumberObject.of(1).asExpression(Expression.Position.EMPTY)))
                            : new UnaryExpression.Subtract(expression, NumberObject.of(1).asExpression(Expression.Position.EMPTY));
                }

                case SQR_BRACKET_START -> {
                    expression = new GetObjectExpression(expression, parseExpression(this.matcher.peek(), variableScope), Expression.Position.from(token, script));

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

                                expression = new CallFunctionException(expression, args.toArray(new Expression[0]), Expression.Position.from(token, script));
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
                                args.add(parseExpression(nexter, variableScope));
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

    private void parseVariableDefinition(Consumer<Expression> consumer, boolean isFinal, VariableId2IntMapper scope) throws UnexpectedTokenException, InvalidTokenException {
        var id = this.matcher.peek();
        var value = XObject.NULL.asExpression(Expression.Position.EMPTY);
        if (id.type() == Tokenizer.TokenType.IDENTIFIER) {
            while (true) {
                var next = this.matcher.peek();
                if (next.type() == Tokenizer.TokenType.SET) {
                    value = parseExpression(scope);
                } else if (next.type() == Tokenizer.TokenType.COMMA) {
                    consumer.accept(new DefineVariableExpression((String) id.value(), scope.declare((String) id.value()), value, isFinal, Expression.Position.betweenIn(id, next, script)));
                    id = this.matcher.peek();
                    if (id.type() != Tokenizer.TokenType.IDENTIFIER) {
                        throw new UnexpectedTokenException(id, Tokenizer.TokenType.IDENTIFIER);
                    }

                    value = XObject.NULL.asExpression(Expression.Position.EMPTY);
                } else {
                    this.matcher.back();
                    consumer.accept(new DefineVariableExpression((String) id.value(), scope.declare((String) id.value()), value, isFinal, Expression.Position.betweenIn(id, next, script)));
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
