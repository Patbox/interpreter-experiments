package eu.pb4.lang.object;

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
    public XObject<?> get(XObject<?> key) {
        return this.map.getOrDefault(key, XObject.NULL);
    }

    @Override
    public void set(XObject<?> key, XObject<?> object) {
        this.map.put(key, object);
    }

    @Override
    public XObject<?> get(String key) {
        return switch (key) {
            case "length", "size" -> new NumberObject(this.map.size());
            case "values" -> new ListObject(this.map.values());
            case "keys" -> new ListObject(this.map.keySet());
            case "entries" -> {
                var list = new ListObject();

                for (var entry : this.map.entrySet()) {
                    var entryList = new ListObject();
                    entryList.add(entry.getKey());
                    entryList.add(entry.getValue());
                    list.asJava().add(entryList);
                }

                yield list;
            }

            default -> super.get(key);
        };
    }

    @Override
    public @Nullable Map<XObject<?>, XObject<?>> asJava() {
        return this.map;
    }
}
