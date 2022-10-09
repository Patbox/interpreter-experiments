package eu.pb4.lang;

import eu.pb4.lang.exception.ScriptConsumer;
import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.ExpressionBuilder;
import eu.pb4.lang.parser.ExpressionMatcher;
import eu.pb4.lang.parser.StringReader;
import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.util.ObjectBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Runtime {
    private final ObjectScope scope = new ObjectScope(this, null, ObjectScope.Type.GLOBAL);

    private final List<Function<String, @Nullable XObject<?>>> importResolvers = new ArrayList<>();
    private final Map<String, XObject<?>> cachedImports = new HashMap<>();

    public ObjectScope getScope() {
        return scope;
    }

    public RunResult runAndStoreExports(String path, String input) {
        var result = this.run(input);
        this.cachedImports.put(path, result.scope.getExportObject());
        return result;
    }

    public RunResult run(String input) {
        try {
            var tokens = new Tokenizer(new StringReader(input)).getTokens();

            var list = new ExpressionBuilder(new ExpressionMatcher(tokens)).build();

            XObject<?> lastObject = XObject.NULL;
            var scope = new ObjectScope(this, this.scope, ObjectScope.Type.SCRIPT);

            for (var expression : list) {
                lastObject = expression.execute(scope);

                if (lastObject instanceof ForceReturnObject forceReturnObject) {
                    return new RunResult(forceReturnObject.asJava(), scope);
                }
            }

            return new RunResult(lastObject, scope);
        } catch (Throwable e) {
            if (e instanceof ScriptConsumer consumer) {
                consumer.supplyInput(input);
            }
            e.printStackTrace();
        }

        return new RunResult(XObject.NULL, scope);
    }

    public void defaultGlobals() {
        scope.declareVariable("print", JavaFunctionObject.ofVoid((args) -> {
            for (var arg : args) {
                System.out.println(arg.asString());
            }
        }));

        scope.declareVariable("List", new JavaFunctionObject((args) -> {
            var list = new ListObject();
            for (var arg : args) {
                list.asJava().add(arg);
            }

            return list;
        }));

        scope.declareVariable("Map", new JavaFunctionObject((args) -> new MapObject()));
        scope.declareVariable("Object", new JavaFunctionObject((args) -> new StringMapObject()));

        scope.declareVariable("Math", new ObjectBuilder()
                        .twoArgRet("min", (a, b) -> new NumberObject(Math.min(a.asNumber(), b.asNumber())))
                        .twoArgRet("max", (a, b) -> new NumberObject(Math.max(a.asNumber(), b.asNumber())))
                        .oneArgRet("round", (a) -> new NumberObject(Math.round(a.asNumber())))
                        .oneArgRet("floor", (a) -> new NumberObject(Math.floor(a.asNumber())))
                        .oneArgRet("ceil", (a) -> new NumberObject(Math.ceil(a.asNumber())))
                        .oneArgRet("sin", (a) -> new NumberObject(Math.sin(a.asNumber())))
                        .oneArgRet("cos", (a) -> new NumberObject(Math.cos(a.asNumber())))
                        .noArg("random", () -> new NumberObject(Math.random()))
                        .put("PI",  new NumberObject(Math.PI))
                        .put("TAU", new NumberObject(Math.PI * 2))
                .build());

        scope.declareVariable("Runtime", new ObjectBuilder()
                        .oneArg("run", x -> this.run(x.asString()))
                        .noArg("currentTimeMillis", () -> System.currentTimeMillis())
                .build());

        scope.declareVariable("Global", scope);
    }

    public void registerImporter(Function<String, @Nullable XObject<?>> importer) {
        this.importResolvers.add(importer);
    }

    //@Nullable
    public XObject<?> tryImporting(String path) {
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

    public record RunResult(XObject<?> object, ObjectScope scope) {}
}
