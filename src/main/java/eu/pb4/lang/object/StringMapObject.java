package eu.pb4.lang.object;

import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StringMapObject extends XObject<Map<String, XObject<?>>>{
    private Map<String, XObject<?>> map = new HashMap<>();

    @Override
    public String asString() {
        var builder = new StringBuilder();

        builder.append("<Object {");

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

        builder.append("}>");

        return builder.toString();
    }

    @Override
    public String type() {
        return "object";
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) {
        return this.map.getOrDefault(key, XObject.NULL);
    }

    @Override
    public XObject<?> get(ObjectScope scope, XObject<?> key, Expression.Position info) {
        return this.get(scope, key.asString(), info);
    }

    @Override
    public void set(ObjectScope scope, String key, XObject<?> object, Expression.Position info) {
        this.map.put(key, object);
    }

    @Override
    public void set(ObjectScope scope, XObject<?> key, XObject<?> object, Expression.Position info) {
        this.set(scope, key.asString(), object, info);
    }

    @Override
    public @Nullable Map<String, XObject<?>> asJava() {
        return this.map;
    }
}
