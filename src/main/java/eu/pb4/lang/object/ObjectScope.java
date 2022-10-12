package eu.pb4.lang.object;

import eu.pb4.lang.Runtime;
import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ObjectScope extends XObject<Map<String, ObjectScope.ScopeVariable>> {
    @Nullable
    private final ObjectScope parentScope;
    private final Type type;
    private final Runtime runtime;

    private final Map<String, ScopeVariable> variables = new IdentityHashMap<>();
    //private final Map<String, ScopeVariable> initialVariables;

    private boolean writable = true;

    @Nullable
    private final Map<String, XObject<?>> exports;
    private XObject<?> exportObject;

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

        this.exportObject = XObject.NULL;

        /*if (this.parentScope != null) {
            this.initialVariables = new Object2ObjectOpenHashMap<>(parentScope.variables);
        } else {
            this.initialVariables = Map.of();
        }
        this.variables.putAll(this.initialVariables);*/
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
        if (key.isEmpty() && this.exports.isEmpty()) {
            this.exportObject = value;
        } else {
            if (!(this.exportObject instanceof StaticStringMapObject x && x.asJava() == this.exports)) {
                this.exportObject = new StaticStringMapObject(this.exports);
            }
        }
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

    public void quickSetVariable(String name, XObject<?> value) {
        this.variables.put(name, new ScopeVariable(name, this, true, value));
    }

    public void declareVariable(String name, XObject<?> value, boolean readOnly) {
        if (this.writable) {
            var obj = this.variables.get(name);

            if (obj == null || obj.scope != this) {
                this.variables.put(name, new ScopeVariable(name, this, readOnly, value));
            } else {
                throw new RuntimeException(name + " is already defined in this scope!");
            }
        } else {
            throw new RuntimeException("Scope isn't writable");
        }
    }

    public void setVariable(String name, XObject<?> value) {
        if (this.writable) {
            var obj = this.variables.get(name);

            if (obj != null) {
                obj.set(value);
            } else if (this.parentScope != null) {
                this.parentScope.setVariable(name, value);
            } else {
                throw new RuntimeException(name + " isn't defined in this scope!");
            }
        } else {
            throw new RuntimeException("Scope isn't writable");
        }
    }

    public XObject<?> getVariable(String name) {
        var obj = this.variables.get(name);

        if (obj != null) {
            return obj.get();
        } else if (this.parentScope != null) {
            return this.parentScope.getVariable(name);
        }

        throw new RuntimeException(name + " isn't defined in this scope!");
    }

    public void clear() {
        this.variables.clear();
    }

    @Override
    public @Nullable Map<String, ScopeVariable> asJava() {
        return this.variables;
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        try {
            return this.getVariable(key);
        } catch (Exception e) {
            throw new InvalidOperationException(info, e.getMessage());
        }
    }

    @Override
    public void set(ObjectScope scope, String key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        try {
            this.setVariable(key, object);
        } catch (Exception e) {
            throw new InvalidOperationException(info, e.getMessage());
        }
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

    public class ScopeVariable {
        public final ObjectScope scope;
        public final boolean readonly;
        public final String name;
        private XObject<?> object;

        public ScopeVariable(String name, ObjectScope scope, boolean readonly, XObject<?> object) {
            this.name = name;
            this.scope = scope;
            this.readonly = readonly;
            this.object = object;
        }

        public void set(XObject<?> object) {
            if (this.readonly) {
                throw new RuntimeException(name + " is readonly!");

            }
            this.object = object;
        }

        public XObject<?> get() {
            return this.object;
        }
    }
}
