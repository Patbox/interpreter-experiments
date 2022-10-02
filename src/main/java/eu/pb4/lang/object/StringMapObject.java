package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StringMapObject extends XObject<Map<String, XObject<?>>> {
    private final Map<String, XObject<?>> map ;

    public StringMapObject(Map<String, XObject<?>> map) {
        this.map = map;
    }

    @Override
    public XObject<?> get(String key) {
        var x = this.map.get(key);
        if (x != null) {
            return x;
        }
        return super.get(key);
    }

    @Nullable
    @Override
    public Map<String, XObject<?>> asJava() {
        return this.map;
    }

    @Override
    public String asString() {
        var builder = new StringBuilder();

        builder.append("<[");

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

        builder.append("]>");

        return builder.toString();
    }
}
