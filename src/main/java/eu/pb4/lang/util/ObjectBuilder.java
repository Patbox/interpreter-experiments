package eu.pb4.lang.util;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.object.JavaFunctionObject;
import eu.pb4.lang.object.StaticStringMapObject;
import eu.pb4.lang.object.XObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ObjectBuilder {
    private final Map<String, XObject<?>> map = new HashMap<>();

    public ObjectBuilder put(String name, XObject<?> value) {
        this.map.put(name, value);
        return this;
    }

    public ObjectBuilder noArg(String name, Runnable function) {
        this.map.put(name, new JavaFunctionObject((scope, args, info) -> {
            function.run();
            return XObject.NULL;
        }));
        return this;
    }

    public ObjectBuilder noArg(String name, Supplier<XObject> function) {
        this.map.put(name, new JavaFunctionObject((scope, args, info) -> {
            return function.get();
        }));
        return this;
    }

    public ObjectBuilder oneArg(String name, Consumer<XObject<?>> function) {
        this.map.put(name, new JavaFunctionObject((scope, args, info) -> {
            function.accept(args[0]);
            return XObject.NULL;
        }));
        return this;
    }

    public ObjectBuilder oneArgRet(String name, Function function) {
        this.map.put(name, new JavaFunctionObject((scope, args, info) -> function.apply(args[0], info)));
        return this;
    }

    public ObjectBuilder twoArgRet(String name, BiFunction function) {
        this.map.put(name, new JavaFunctionObject((scope, args, info) -> function.apply(args[0], args[1], info)));

        return this;
    }

    public ObjectBuilder varArg(String name, JavaFunctionObject.Function function) {
        this.map.put(name, new JavaFunctionObject(function));
        return this;
    }

    public XObject<?> build() {
        return new StaticStringMapObject(this.map);
    }


    public interface Function {
        XObject<?> apply(XObject<?> object, Expression.Position info) throws InvalidOperationException;
    }

    public interface BiFunction {
        XObject<?> apply(XObject<?> object, XObject<?> object2, Expression.Position info) throws InvalidOperationException;
    }
}
