package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.util.GenUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapObject extends XObject<Map<XObject<?>, XObject<?>>>{
    private Map<XObject<?>, XObject<?>> map = new HashMap<>();
    private XObject<?> functionGet = new JavaFunctionObject((x, a, i) -> {
        GenUtils.argumentCount(a, 1, i);
        if (a.length == 2) {
            return this.map.getOrDefault(a[0], a[1]);
        }
        return this.map.get(a[0]);
    });

    @Override
    public String asString() {
        var builder = new StringBuilder();

        builder.append("<Map {");

        var iterator = this.map.entrySet().iterator();

        while (iterator.hasNext()) {
            var x = iterator.next();
            builder.append(x.getKey().asString());
            builder.append(" -> ");
            builder.append(x.getValue().asString());

            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append("}>");

        return builder.toString();
    }

    @Override
    public XObject<?> get(ObjectScope scope, XObject<?> key, Expression.Position info) {
        return this.map.getOrDefault(key, XObject.NULL);
    }

    @Override
    public void set(ObjectScope scope, XObject<?> key, XObject<?> object, Expression.Position info) {
        this.map.put(key, object);
    }

    @Override
    public Iterator<XObject<?>> iterator(ObjectScope scope, Expression.Position info) throws InvalidOperationException {
        return new MapIterator(this.map.entrySet().iterator());
    }

    @Override
    public String type() {
        return "map";
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "length", "size" -> NumberObject.of(this.map.size());
            case "values" -> new ListObject(this.map.values());
            case "keys" -> new ListObject(this.map.keySet());
            case "get" -> this.functionGet;
            case "entries" -> {
                var list = new ListObject();

                for (var entry : this.map.entrySet()) {
                    var entryList = new ListObject();
                    entryList.asJava().add(entry.getKey());
                    entryList.asJava().add(entry.getValue());
                    list.asJava().add(entryList);
                }

                yield list;
            }

            default -> super.get(scope, key, info);
        };
    }

    @Override
    public @Nullable Map<XObject<?>, XObject<?>> asJava() {
        return this.map;
    }

    public static XObject<?> create(ObjectScope scope, XObject<?>[] xObjects, Expression.Position position) {
        var map = new MapObject();

        for (var arg : xObjects) {
            if (arg instanceof MapObject mapObject) {
                map.map.putAll(mapObject.map);
            } else if (arg instanceof StringMapObject mapObject) {
                mapObject.asJava().forEach((a, b) -> map.map.put(new StringObject(a), b));
            } else if (arg instanceof ListObject listObject) {
                for (var i = 0; i < listObject.asJava().size(); i++) {
                    map.map.put(NumberObject.of(i), listObject.asJava().get(i));
                }
            }
        }

        return map;
    }

    private record MapIterator(Iterator<Map.Entry<XObject<?>, XObject<?>>> iterator) implements Iterator<XObject<?>> {
        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public XObject<?> next() {
            var next = this.iterator.next();
            var list = new ListObject();
            list.asJava().add(next.getKey());
            list.asJava().add(next.getValue());
            return list;
        }
    }
}
