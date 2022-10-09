package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MapObject extends XObject<Map<XObject<?>, XObject<?>>>{
    private Map<XObject<?>, XObject<?>> map = new HashMap<>();

    @Override
    public String asString() {
        var builder = new StringBuilder();

        builder.append("<[");

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

        builder.append("]>");

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
    public String type() {
        return "map";
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "length", "size" -> new NumberObject(this.map.size());
            case "values" -> new ListObject(this.map.values());
            case "keys" -> new ListObject(this.map.keySet());
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
}
