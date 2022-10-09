package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StringMapObject extends XObject<Map<String, XObject<?>>>{
    private Map<String, XObject<?>> map = new HashMap<>();

    @Override
    public String asString() {
        var builder = new StringBuilder();

        builder.append("<[{");

        var iterator = this.map.entrySet().iterator();

        while (iterator.hasNext()) {
            var x = iterator.next();
            builder.append(x.getKey());
            builder.append(" -> ");
            builder.append(x.getValue().asString());

            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append("}]>");

        return builder.toString();
    }

    @Override
    public XObject<?> get(String key) {
        return this.map.getOrDefault(key, XObject.NULL);
    }

    @Override
    public XObject<?> get(XObject<?> key) {
        return this.get(key.asString());
    }

    @Override
    public void set(String key, XObject<?> object) {
        this.map.put(key, object);
    }

    @Override
    public void set(XObject<?> key, XObject<?> object) {
        this.set(key.asString(), object);
    }

    @Override
    public @Nullable Map<String, XObject<?>> asJava() {
        return this.map;
    }
}
