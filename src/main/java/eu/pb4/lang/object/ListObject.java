package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListObject extends XObject<List<XObject<?>>> {
    private final List<XObject<?>> list = new ArrayList<>();

    private final JavaFunctionObject addFunc = JavaFunctionObject.ofVoid((scope, args, info) -> {
        for (var arg : args) {
            this.list.add(arg);
        }
    });

    private final JavaFunctionObject removeFunc = JavaFunctionObject.ofVoid((scope, args, info) -> {
        for (var arg : args) {
            this.list.remove(arg);
        }
    });

    private XObject<?> forEachFunc = JavaFunctionObject.ofVoid((o, x, i) -> {
        for (var e : this.list) {
            x[0].call(o, new XObject[]{ e }, i);
        }
    });


    public ListObject() {}
    public ListObject(Collection<XObject<?>> values) {
        this.list.addAll(values);
    }

    @Override
    public String type() {
        return "list";
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
    public XObject<?> get(ObjectScope scope, XObject<?> key, Expression.Position info) throws InvalidOperationException {
        if (key instanceof NumberObject numberObject) {
            return this.list.get((int) numberObject.value());
        }
        return super.get(scope, key, info);
    }

    @Override
    public void set(ObjectScope scope, XObject<?> key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        int value = object.asInt(info);
        while (this.list.size() <= value) {
            this.list.add(XObject.NULL);
        }

        this.list.set(value, object);
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "length" -> NumberObject.of(this.list.size());
            case "add" -> this.addFunc;
            case "remove" -> this.removeFunc;
            case "forEach" -> this.forEachFunc;

            default -> super.get(scope, key, info);
        };
    }

    @Override
    public Iterator<XObject<?>> iterator(ObjectScope scope, Expression.Position info) throws InvalidOperationException {
        return this.list.iterator();
    }

    @Override
    public @Nullable List<XObject<?>> asJava() {
        return this.list;
    }

    public static XObject<?> create(ObjectScope scope, XObject<?>[] xObjects, Expression.Position position) throws InvalidOperationException {
        var map = new ListObject();

        for (var arg : xObjects) {
            var i = arg.iterator(scope, position);
            while (i.hasNext()) {
                map.list.add(i.next());
            }
        }

        return map;
    }
}
