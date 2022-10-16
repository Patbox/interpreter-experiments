package eu.pb4.lang.runtime;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.exception.InvalidTokenException;
import eu.pb4.lang.exception.ScriptConsumer;
import eu.pb4.lang.exception.UnexpectedTokenException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.libs.FileSystemLibrary;
import eu.pb4.lang.libs.GZIPLibrary;
import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.ExpressionMatcher;
import eu.pb4.lang.parser.StringReader;
import eu.pb4.lang.parser.TokenReader;
import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.util.GenUtils;
import eu.pb4.lang.util.ObjectBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Runtime {
    private final List<Function<String, @Nullable XObject<?>>> importResolvers = new ArrayList<>();
    private final Map<String, XObject<?>> cachedImports = new HashMap<>();
    private final Map<String, XObject<?>> globals = new HashMap<>();
    private final List<AsyncState> asyncStates = new ArrayList<>();
    private final List<State> timeout = new ArrayList<>();
    private final List<State> interval = new ArrayList<>();
    private long lastTickTime;
    private int intervalId;

    public static byte[] buildByteCode(String input) throws IOException, InvalidTokenException, UnexpectedTokenException {
        try {
            var tokens = new Tokenizer(new StringReader(input)).getTokens();

            var list = new ExpressionMatcher(new TokenReader(tokens), input).build();

            var out = new ByteArrayOutputStream();

            {
                var bb = new ByteArrayOutputStream();
                var objects = new StaticObjectConsumer();
                var stream = new DataOutputStream(bb);

                for (var expr : list) {
                    expr.writeByteCode(stream, objects);
                }

                objects.write(new DataOutputStream(out));
                out.write(bb.toByteArray());
            }

            return out.toByteArray();
        } catch (Throwable consumer) {
            if (consumer instanceof ScriptConsumer consumer1) {
                consumer1.supplyInput(input);
            }
            throw consumer;
        }

    }

    public RunResult importAndRun(String path, String input) {
        var result = this.run(input);
        this.cachedImports.put(path, result.scope.getExportObject());
        return result;
    }

    public RunResult run(String input) {
        try {
            return execute(buildByteCode(input));
        } catch (Throwable e) {
            if (e instanceof ScriptConsumer consumer) {
                consumer.supplyInput(input);
            }
            e.printStackTrace();
        }

        return new RunResult(XObject.NULL, null);
    }

    public void defaultGlobals() {
        this.setGlobal("print", JavaFunctionObject.ofVoid((scope, args, info) -> {
            for (var arg : args) {
                System.out.println(arg.asString());
            }
        }));

        this.setGlobal("wait", JavaFunctionObject.ofVoid((scope, args, info) -> {
            try {
                Thread.sleep(args[0].asInt(info));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        this.setGlobal("List", new JavaFunctionObject(ListObject::create));
        this.setGlobal("String", new JavaFunctionObject(StringObject::create));
        this.setGlobal("BitSet", new JavaFunctionObject(BitSetObject::create));
        this.setGlobal("Map", new JavaFunctionObject(MapObject::create));
        this.setGlobal("Object", new JavaFunctionObject((scope, args, info) -> new StringMapObject()));
        this.setGlobal("ByteArrayWriter", new JavaFunctionObject(ByteArrayWriterObject::create));
        this.setGlobal("ByteArray", new JavaFunctionObject(ByteArrayObject::create));

        this.setGlobal("Math", new ObjectBuilder()
                .twoArgRet("min", (a, b, i) -> NumberObject.of(Math.min(a.asDouble(i), b.asDouble(i))))
                .twoArgRet("max", (a, b, i) -> NumberObject.of(Math.max(a.asDouble(i), b.asDouble(i))))
                .oneArgRet("round", (a, i) -> NumberObject.of(Math.round(a.asDouble(i))))
                .oneArgRet("floor", (a, i) -> NumberObject.of(Math.floor(a.asDouble(i))))
                .oneArgRet("ceil", (a, i) -> NumberObject.of(Math.ceil(a.asDouble(i))))
                .oneArgRet("sin", (a, i) -> NumberObject.of(Math.sin(a.asDouble(i))))
                .oneArgRet("cos", (a, i) -> NumberObject.of(Math.cos(a.asDouble(i))))
                .noArg("random", () -> NumberObject.of(Math.random()))
                .put("PI", NumberObject.of(Math.PI))
                .put("TAU", NumberObject.of(Math.PI * 2))
                .build());

        this.setGlobal("Runtime", new ObjectBuilder()
                .oneArgRet("run", (x, i) -> this.run(x.asString()).object())
                .noArg("currentTimeMillis", () -> NumberObject.of(System.currentTimeMillis()))
                .varArg("interval", (scope, args, info) -> {

                    var id = this.intervalId++;
                    this.interval.add(new State(id, System.currentTimeMillis() + args[0].asInt(info), args[0].asInt(info), args[1], scope));

                    return NumberObject.of(id);
                })

                .varArg("timeout", (scope, args, info) -> {
                    var id = this.intervalId++;
                    this.timeout.add(new State(id, System.currentTimeMillis() + args[0].asInt(info), args[0].asInt(info), args[1], scope));

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

        this.setGlobal("FS", FileSystemLibrary.build());
        this.setGlobal("GZIP", GZIPLibrary.build());

        this.setGlobal("Global", new StaticStringMapObject(this.globals));
    }

    public void setGlobal(String key, XObject<?> obj) {
        this.globals.put(key, obj);
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
                var result = this.execute(new ByteArrayReader(state.bytecode), state.scope);
                state.lastObject = result.object;

                state.future.complete(result);
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
                    state.function.call(state.scope, new XObject[0], Expression.Position.EMPTY);
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
                    state.function.call(state.scope, new XObject[0], Expression.Position.EMPTY);
                } catch (InvalidOperationException e) {
                    e.printStackTrace();
                }
                this.interval.remove(state);
            }
        }

        this.lastTickTime = System.currentTimeMillis();
    }

    public RunResult execute(byte[] bytecode) throws InvalidOperationException, IOException {
        final var buffer = new ByteArrayReader(bytecode);
        final var constants = StaticObjectConsumer.decode(buffer);

        return execute(buffer, new ObjectScope(this, null, constants, ObjectScope.Type.SCRIPT));
    }

    public RunResult execute(final ByteArrayReader buffer, ObjectScope scope) throws InvalidOperationException, IOException {
        final var stack = new RuntimeStack();
        stack.scope = scope;
        stack.printTime = false;
        stack.printOpcodes = false;

        final var opcodeTime = new long[Opcodes.CALLS.length];
        final var opcodeInvocation = new int[Opcodes.CALLS.length];

        while (buffer.hasMore()) {
            var t = System.nanoTime();
            stack.instructionStartNanoTime = t;
            var opcode = buffer.readByte();
            opcodeInvocation[opcode]++;
            if (stack.printOpcodes) {
                System.out.println(opcode + " | " + Opcodes.values()[opcode]);
            }
            Opcodes.CALLS[opcode].call(this, stack.scope, stack, buffer);
            opcodeTime[opcode] += System.nanoTime() - t;
            if (stack.finished == true) {
                break;
            }
        }

        if (stack.printTime) {
            System.out.println("OPT");
            for (var opcode : Opcodes.values()) {
                if (opcodeTime[opcode.ordinal()] > 1000000) {
                    System.out.println(opcode + " | NT: " + opcodeTime[opcode.ordinal()] + " | Inv: " + opcodeInvocation[opcode.ordinal()] + " | NT/Inv: " + (opcodeTime[opcode.ordinal()] / opcodeInvocation[opcode.ordinal()])  );
                }
            }

            //System.out.println("ScopesUps: " + scopeUp);
            //System.out.println("ScopesDowns: " + scopeDown);

        }

        return new RunResult(stack.lastObject, scope);
    }


    public CompletableFuture<RunResult> executeAsync(List<Expression> expr, ObjectScope scope) throws InvalidOperationException {
        var state = new AsyncState(null, scope);

        //this.asyncStates.add(state);

        return state.future;
    }

    @Nullable
    public XObject<?> getGlobal(String name) {
        return this.globals.get(name);
    }

    public record RunResult(XObject<?> object, ObjectScope scope) {
    }

    private class State {
        protected final int id;
        protected final int time;
        protected final ObjectScope scope;
        protected final XObject<?> function;
        protected long activate;

        protected State(int id, long timeToActivate, int time, XObject<?> function, ObjectScope scope) {
            this.id = id;
            this.activate = timeToActivate;
            this.time = time;
            this.function = function;
            this.scope = scope;
        }
    }

    private class AsyncState {
        protected final ObjectScope scope;
        protected final byte[] bytecode;
        protected CompletableFuture<RunResult> future = new CompletableFuture<RunResult>();
        protected XObject<?> lastObject = XObject.NULL;

        protected AsyncState(byte[] bytecode, ObjectScope scope) {
            this.bytecode = bytecode;
            this.scope = scope;
        }
    }
}
