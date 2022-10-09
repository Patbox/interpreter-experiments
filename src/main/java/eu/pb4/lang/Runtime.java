package eu.pb4.lang;

import eu.pb4.lang.exception.ScriptConsumer;
import eu.pb4.lang.object.*;
import eu.pb4.lang.parser.ExpressionBuilder;
import eu.pb4.lang.parser.ExpressionMatcher;
import eu.pb4.lang.parser.StringReader;
import eu.pb4.lang.parser.Tokenizer;
import eu.pb4.lang.util.ObjectBuilder;

public class Runtime {
    private final ObjectScope scope = new ObjectScope(null);

    public ObjectScope getScope() {
        return scope;
    }

    public XObject<?> run(String input) {
        try {
            var list = new ExpressionBuilder(new ExpressionMatcher(new Tokenizer(new StringReader(input)).getTokens())).build();

            XObject<?> lastObject = XObject.NULL;
            var scope = new ObjectScope(this.scope);

            for (var expression : list) {
                lastObject = expression.execute(scope);

                if (lastObject instanceof ForceReturnObject forceReturnObject) {
                    return forceReturnObject.asJava();
                }
            }

            return lastObject;
        } catch (Throwable e) {
            if (e instanceof ScriptConsumer consumer) {
                consumer.supplyInput(input);
            }
            e.printStackTrace();
        }

        return XObject.NULL;
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
}
