package eu.pb4.lang;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.exception.ScriptConsumer;
import eu.pb4.lang.expression.CallFunctionException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.libs.FileSystemLibrary;
import eu.pb4.lang.libs.GZIPLibrary;
import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.ExpressionMatcher;
import eu.pb4.lang.parser.TokenReader;
import eu.pb4.lang.parser.StringReader;
import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.util.ObjectBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Runtime {
    private final ObjectScope scope = new ObjectScope(this, null, ObjectScope.Type.GLOBAL);

    private final List<Function<String, @Nullable XObject<?>>> importResolvers = new ArrayList<>();
    private final Map<String, XObject<?>> cachedImports = new HashMap<>();
    private List<AsyncState> asyncStates = new ArrayList<>();

    private List<State> timeout = new ArrayList<>();
    private List<State> interval = new ArrayList<>();
    private long lastTickTime;
    private int intervalId;

    public ObjectScope getScope() {
        return scope;
    }

    public RunResult importAndRun(String path, String input) {
        var result = this.run(input);
        this.cachedImports.put(path, result.scope.getExportObject());
        return result;
    }

    public RunResult run(String input) {
        try {
            var tokens = new Tokenizer(new StringReader(input)).getTokens();

            var list = new ExpressionMatcher(new TokenReader(tokens)).build();

            return execute(list);
        } catch (Throwable e) {
            if (e instanceof ScriptConsumer consumer) {
                consumer.supplyInput(input);
            }
            e.printStackTrace();
        }

        return new RunResult(XObject.NULL, scope);
    }

    public void defaultGlobals() {
        scope.quickSetVariable("print", JavaFunctionObject.ofVoid((scope, args, info) -> {
            for (var arg : args) {
                System.out.println(arg.asString());
            }
        }));

        scope.quickSetVariable("wait", JavaFunctionObject.ofVoid((scope, args, info) -> {
            try {
                Thread.sleep(args[0].asInt(info));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        scope.quickSetVariable("List", new JavaFunctionObject(ListObject::create));
        scope.quickSetVariable("String", new JavaFunctionObject(StringObject::create));
        scope.quickSetVariable("BitSet", new JavaFunctionObject(BitSetObject::create));
        scope.quickSetVariable("Map", new JavaFunctionObject(MapObject::create));
        scope.quickSetVariable("Object", new JavaFunctionObject((scope, args, info) -> new StringMapObject()));
        scope.quickSetVariable("ByteArrayWriter", new JavaFunctionObject(ByteArrayWriterObject::create));
        scope.quickSetVariable("ByteArray", new JavaFunctionObject(ByteArrayObject::create));

        scope.quickSetVariable("Math", new ObjectBuilder()
                        .twoArgRet("min", (a, b, i) -> NumberObject.of(Math.min(a.asDouble(i), b.asDouble(i))))
                        .twoArgRet("max", (a, b, i) -> NumberObject.of(Math.max(a.asDouble(i), b.asDouble(i))))
                        .oneArgRet("round", (a, i) -> NumberObject.of(Math.round(a.asDouble(i))))
                        .oneArgRet("floor", (a, i) -> NumberObject.of(Math.floor(a.asDouble(i))))
                        .oneArgRet("ceil", (a, i) -> NumberObject.of(Math.ceil(a.asDouble(i))))
                        .oneArgRet("sin", (a, i) -> NumberObject.of(Math.sin(a.asDouble(i))))
                        .oneArgRet("cos", (a, i) -> NumberObject.of(Math.cos(a.asDouble(i))))
                        .noArg("random", () -> NumberObject.of(Math.random()))
                        .put("PI",  NumberObject.of(Math.PI))
                        .put("TAU", NumberObject.of(Math.PI * 2))
                .build());

        scope.quickSetVariable("Runtime", new ObjectBuilder()
                        .oneArgRet("run", (x, i) -> this.run(x.asString()).object())
                        .noArg("currentTimeMillis", () -> NumberObject.of(System.currentTimeMillis()))
                        .varArg("interval", (scope, args, info) -> {

                            var id = this.intervalId++;
                            this.interval.add(new State(id, System.currentTimeMillis() + args[0].asInt(info), args[0].asInt(info),
                                    List.of(new CallFunctionException(args[1].asExpression(info), new Expression[0], info)), scope));

                            return NumberObject.of(id);
                        })

                        .varArg("timeout", (scope, args, info) -> {
                            var id = this.intervalId++;
                            this.timeout.add(new State(id, System.currentTimeMillis() + args[0].asInt(info), args[0].asInt(info),
                                    List.of(new CallFunctionException(args[1].asExpression(info), new Expression[0], info)), scope));

                            return NumberObject.of(id);
                        })

                        .oneArgRet("clearInterval", (arg, i) -> {
                            var val = arg.asInt(i);
                            return BooleanObject.of(this.interval.removeIf(x -> x.id == val));
                        })
                        .oneArgRet("clearTimeout", (arg, i) -> {
                            var val = arg.asInt(i);
                            return BooleanObject.of(this.timeout.removeIf(x -> x.id == val));
                        })

                .build());

        scope.quickSetVariable("FS", FileSystemLibrary.build());
        scope.quickSetVariable("GZIP", GZIPLibrary.build());

        scope.quickSetVariable("Global", scope);
    }

    public void registerImporter(Function<String, @Nullable XObject<?>> importer) {
        this.importResolvers.add(importer);
    }

    //@Nullable
    public XObject<?> importAndRun(String path) {
        var out = this.cachedImports.get(path);
        if (out != null) {
            return out;
        }

        for (var importer : this.importResolvers) {
            out = importer.apply(path);
            if (out != null) {
                return out;
            }
        }
        return XObject.NULL;
    }

    public void tick() {
        var iterator = this.asyncStates.listIterator();
        while (iterator.hasNext()) {
            var state = iterator.next();

            try {
                while (state.iterator.hasNext()) {
                    state.lastObject = state.iterator.next().execute(state.scope);
                }

                state.future.complete(new RunResult(state.lastObject, state.scope));
            } catch (Throwable e) {
                state.future.completeExceptionally(e);
                e.printStackTrace();
            }
            iterator.remove();
        }

        for (var state : List.copyOf(this.interval)) {
            var delta = state.activate - this.lastTickTime;
            if (delta <= 0) {
                try {
                    this.execute(state.expressions, state.scope);
                    state.activate = System.currentTimeMillis() - delta + state.time;
                } catch (InvalidOperationException e) {
                    this.interval.remove(state);
                    e.printStackTrace();
                }
            }
        }

        for (var state : List.copyOf(this.timeout)) {
            var delta = state.activate - this.lastTickTime;
            if (delta <= 0) {
                try {
                    this.execute(state.expressions, state.scope);
                } catch (InvalidOperationException e) {
                    e.printStackTrace();
                }
                this.interval.remove(state);
            }
        }

        this.lastTickTime = System.currentTimeMillis();
    }

    public RunResult execute(List<Expression> expr) throws InvalidOperationException {
        return execute(expr, this.scope);
    }

    public RunResult execute(List<Expression> expr, ObjectScope scope) throws InvalidOperationException {
        XObject<?> lastObject = XObject.NULL;
        scope = new ObjectScope(this, scope);

        for (var expression : expr) {
            lastObject = expression.execute(scope);

            if (lastObject instanceof ForceReturnObject forceReturnObject) {
                return new RunResult(forceReturnObject.asJava(), scope);
            }
        }

        return new RunResult(lastObject, scope);
    }


    public CompletableFuture<RunResult> executeAsync(List<Expression> expr, ObjectScope scope) throws InvalidOperationException {
        var state = new AsyncState(expr, scope);

        //this.asyncStates.add(state);

        return state.future;
    }

    public record RunResult(XObject<?> object, ObjectScope scope) {}

    private class State {
        protected final int id;
        protected long activate;
        protected final int time;
        protected final ObjectScope scope;
        protected final List<Expression> expressions;

        protected State(int id, long timeToActivate, int time, List<Expression> expressions, ObjectScope scope) {
            this.id = id;
            this.activate = timeToActivate;
            this.time = time;
            this.expressions = expressions;
            this.scope = scope;
        }
    }

    private class AsyncState {
        protected CompletableFuture<RunResult> future = new CompletableFuture<RunResult>();
        protected XObject<?> lastObject = XObject.NULL;
        protected final ObjectScope scope;
        protected final Iterator<Expression> iterator;
        
        protected AsyncState(List<Expression> expressions, ObjectScope scope) {
            this.iterator = expressions.iterator();
            this.scope = scope;
        }
    }
}
