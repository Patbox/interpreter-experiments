package eu.pb4.lang.object;

import eu.pb4.lang.Runtime;
import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ObjectScope extends XObject<ObjectScope.ScopeVariable[]> {
    @Nullable
    private final ObjectScope parentScope;
    private final Type type;
    private final Runtime runtime;
    @Nullable
    private final Map<String, XObject<?>> exports;
    @Nullable
    private ScopeVariable[] initialVariables;
    private ScopeVariable[] localVariables;

    private XObject<?> exportObject;

    public ObjectScope(Runtime runtime, @Nullable ObjectScope parentScope, Type type) {
        if (type != Type.SCRIPT && parentScope == null) {
            throw new IllegalArgumentException("parentScope can't be null for non-global scope!");
        }
        this.parentScope = parentScope;
        this.type = type;
        this.runtime = runtime;
        this.exports = switch (this.type) {
            case SCRIPT -> new HashMap<>();
            case LOCAL -> parentScope.exports;
        };

        this.exportObject = XObject.NULL;

        if (this.parentScope != null) {
            this.initialVariables = Arrays.copyOf(this.parentScope.localVariables, this.parentScope.localVariables.length);
            this.localVariables = Arrays.copyOf(this.parentScope.localVariables, this.parentScope.localVariables.length);
        } else {
            this.initialVariables = null;
            this.localVariables = new ScopeVariable[8];
        }
    }

    public ObjectScope(Runtime runtime, ObjectScope parentScope) {
        this(runtime, parentScope, Type.LOCAL);
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

    public void declareVariable(String name, int id, XObject<?> value, boolean readOnly) {
        if (id == -1) {
            throw new RuntimeException("<<RUNTIME EXCEPTION>> declareVariable " + name + " > " + id);
        }

        var variable = new ScopeVariable(name, this, readOnly, value);
        if (this.localVariables.length <= id) {
            this.localVariables = Arrays.copyOf(this.localVariables, id + 8);
        }
        this.localVariables[id] = variable;

    }

    public void setVariable(String name, int id, XObject<?> value) {
        var obj = this.localVariables[id];
        if (obj != null) {
            obj.set(value);
            return;
        }

        throw new RuntimeException(name + " isn't defined in this scope!");
    }

    public XObject<?> getVariable(String name, int id) {
        if (id != -1) {
            var obj = this.localVariables[id];

            if (obj != null) {
                return obj.get();
            }
        } else {
            var obj = this.runtime.getGlobal(name);

            if (obj != null) {
                return obj;
            }
        }

        throw new RuntimeException(name + " isn't defined in this scope!");
    }

    public void clear() {
        int i = 0;
        for (; i < this.initialVariables.length; i++) {
            this.localVariables[i] = this.initialVariables[i];
        }
        for (; i < this.localVariables.length; i++) {
            this.localVariables[i] = null;
        }
    }

    @Override
    public @Nullable ScopeVariable[] asJava() {
        return this.localVariables;
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        try {
            return this.getVariable(key, -1);
        } catch (Exception e) {
            throw new InvalidOperationException(info, e.getMessage());
        }
    }

    @Override
    public void set(ObjectScope scope, String key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        try {
            this.setVariable(key, -1, object);
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

    public void updateInitialState() {
        if (this.initialVariables.length < this.localVariables.length) {
            this.initialVariables = Arrays.copyOf(this.localVariables, this.localVariables.length);
        } else {
            for (int i = 0; i < this.localVariables.length; i++) {
                this.initialVariables[i] = this.localVariables[i];
            }
        }
    }

    public enum Type {
        SCRIPT, LOCAL
    }

    public static class ScopeVariable {
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
