package eu.pb4.lang.object;

import eu.pb4.lang.Runtime;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ObjectScope extends XObject<Map<String, XObject<?>>> {
    @Nullable
    private final ObjectScope parentScope;
    private final Type type;
    private final Runtime runtime;

    private Map<String, XObject<?>> variables = new HashMap<>();
    private boolean writable = true;

    @Nullable
    private final Map<String, XObject<?>> exports;
    private final XObject<?> exportObject;

    public ObjectScope(Runtime runtime, @Nullable ObjectScope parentScope, Type type) {
        if (type != Type.GLOBAL && parentScope == null) {
            throw new IllegalArgumentException("parentScope can't be null for non-global scope!");
        }
        this.parentScope = parentScope;
        this.type = type;
        this.runtime = runtime;
        this.exports = switch (this.type) {
            case GLOBAL -> Map.of();
            case SCRIPT -> new HashMap<>();
            case LOCAL -> parentScope.exports;
        };

        this.exportObject = new StaticStringMapObject(this.exports);
    }

    public ObjectScope(Runtime runtime, ObjectScope parentScope) {
        this(runtime, parentScope, switch (parentScope.getType()) {
            case GLOBAL -> Type.SCRIPT;
            default -> Type.LOCAL;
        });
    }

    public ObjectScope(ObjectScope parentScope) {
        this(parentScope.runtime, parentScope);
    }

    @Nullable
    public ObjectScope getParent() {
        return this.parentScope;
    }

    public Runtime getRuntime() {
        return this.runtime;
    }

    public void addExport(String key, XObject<?> value) {
        this.exports.put(key, value);
    }

    public XObject<?> getExport(String key) {
        return this.exports.getOrDefault(key, XObject.NULL);
    }

    public Type getType() {
        return this.type;
    }

    public void freeze() {
        this.writable = false;
    }

    public void declareVariable(String name, XObject<?> value) {
        if (this.writable) {
            this.variables.put(name, value);
        }
    }

    public void setVariable(String name, XObject<?> value) {
        if (this.writable) {
            if (this.variables.containsKey(name)) {
                this.variables.put(name, value);
            } else if (this.parentScope != null) {
                this.parentScope.setVariable(name, value);
            }
        }
    }

    public XObject<?> getVariable(String name) {
        if (this.variables.containsKey(name)) {
            return this.variables.get(name);
        } else if (this.parentScope != null) {
            return this.parentScope.getVariable(name);
        }

        return XObject.NULL;
    }

    @Override
    public @Nullable Map<String, XObject<?>> asJava() {
        return this.variables;
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) {
        return this.getVariable(key);
    }

    @Override
    public String asString() {
        return "<object scope>";
    }

    @Override
    public String type() {
        return "object scope";
    }

    public XObject<?> getExportObject() {
        return this.exportObject;
    }

    public enum Type {
        GLOBAL,
        SCRIPT,
        LOCAL
    }
}
