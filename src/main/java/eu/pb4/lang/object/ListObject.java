package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListObject extends XObject<List<XObject<?>>> {
    private final List<XObject<?>> list = new ArrayList<>();

    private final JavaFunctionObject addFunc = JavaFunctionObject.ofVoid(args -> {
        for (var arg : args) {
            this.list.add(arg);
        }
    });

    private final JavaFunctionObject removeFunc = JavaFunctionObject.ofVoid(args -> {
        for (var arg : args) {
            this.list.remove(arg);
        }
    });

    private XObject<?> forEachFunc = JavaFunctionObject.ofVoid(x -> this.list.forEach(y -> x[0].call(y)));


    public ListObject() {}
    public ListObject(Collection<XObject<?>> values) {
        this.list.addAll(values);
    }

    @Override
    public String asString() {
        var builder = new StringBuilder();

        builder.append("<[");

        var iterator = this.list.iterator();

        while (iterator.hasNext()) {
            builder.append(iterator.next().asString());

            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append("]>");

        return builder.toString();
    }

    @Override
    public XObject<?> get(XObject<?> key) {
        if (key instanceof NumberObject numberObject) {
            return this.list.get((int) numberObject.value());
        }
        return super.get(key);
    }

    @Override
    public void set(XObject<?> key, XObject<?> object) {
        if (key instanceof NumberObject numberObject) {
            int value = (int) numberObject.value();
            while (this.list.size() <= value) {
                this.list.add(XObject.NULL);
            }

            this.list.set(value, object);
        }

        super.set(key, object);
    }

    @Override
    public XObject<?> get(String key) {
        return switch (key) {
            case "length" -> new NumberObject(this.list.size());
            case "add" -> this.addFunc;
            case "remove" -> this.removeFunc;
            case "forEach" -> this.forEachFunc;

            default -> super.get(key);
        };
    }

    @Override
    public @Nullable List<XObject<?>> asJava() {
        return this.list;
    }
}
