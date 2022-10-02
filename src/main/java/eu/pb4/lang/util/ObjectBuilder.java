package eu.pb4.lang.util;

import eu.pb4.lang.object.JavaFunctionObject;
import eu.pb4.lang.object.StringMapObject;
import eu.pb4.lang.object.XObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ObjectBuilder {
    private final Map<String, XObject<?>> map = new HashMap<>();

    public ObjectBuilder put(String name, XObject<?> value) {
        this.map.put(name, value);
        return this;
    }

    public ObjectBuilder noArg(String name, Runnable function) {
        this.map.put(name, new JavaFunctionObject(x -> {
            function.run();
            return XObject.NULL;
        }));
        return this;
    }

    public ObjectBuilder noArg(String name, Supplier<XObject> function) {
        this.map.put(name, new JavaFunctionObject(x -> {
            return function.get();
        }));
        return this;
    }

    public ObjectBuilder oneArg(String name, Consumer<XObject<?>> function) {
        this.map.put(name, new JavaFunctionObject(x -> {
            function.accept(x[0]);
            return XObject.NULL;
        }));
        return this;
    }

    public ObjectBuilder oneArgRet(String name, Function<XObject<?>, XObject<?>> function) {
        this.map.put(name, new JavaFunctionObject(x -> function.apply(x[0])));
        return this;
    }

    public ObjectBuilder twoArgRet(String name, BiFunction<XObject<?>, XObject<?>, XObject<?>> function) {
        this.map.put(name, new JavaFunctionObject(x -> function.apply(x[0], x[1])));

        return this;
    }

    public ObjectBuilder varArg(String name, Function<XObject<?>[], XObject<?>> function) {
        this.map.put(name, new JavaFunctionObject(function));
        return this;
    }

    public XObject<?> build() {
        return new StringMapObject(this.map);
    }
}
