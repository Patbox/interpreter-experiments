package eu.pb4.lang.runtime;

import eu.pb4.lang.object.StaticStringMapObject;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ObjectScope extends XObject<ObjectScope.ScopeVariable[]> {
    @Nullable
    private final ObjectScope parentScope;
    private final Type type;
    private final Runtime runtime;
    @Nullable
    private final Map<String, XObject<?>> exports;


    private ScopeVariable[][] storedVariables = new ScopeVariable[8][];
    private int storedStateId = -1;

    private ScopeVariable[] localVariables;

    private XObject<?> exportObject;
    private final XObject<?>[] constants;

    public ObjectScope(Runtime runtime, @Nullable ObjectScope parentScope, XObject<?>[] constants, Type type) {
        if (type != Type.SCRIPT && parentScope == null) {
            throw new IllegalArgumentException("parentScope can't be null for non-global scope!");
        }
        this.constants = constants;
        this.parentScope = parentScope;
        this.type = type;
        this.runtime = runtime;
        this.exports = switch (this.type) {
            case SCRIPT -> new Object2ObjectOpenHashMap<>();
            case LOCAL -> parentScope.exports;
        };

        this.exportObject = XObject.NULL;

        if (this.parentScope != null) {
            this.localVariables = new ScopeVariable[this.parentScope.localVariables.length];
            System.arraycopy(this.parentScope.localVariables, 0, this.localVariables, 0, this.parentScope.localVariables.length);
        } else {
            this.localVariables = new ScopeVariable[16];
        }
    }

    public ObjectScope(Runtime runtime, ObjectScope parentScope) {
        this(runtime, parentScope, parentScope.constants, Type.LOCAL);
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
            var old = this.localVariables;
            this.localVariables = new ScopeVariable[id + 16];
            System.arraycopy(old, 0, this.localVariables, 0, old.length);

        }
        this.localVariables[id] = variable;

    }

    public void setVariable(int id, XObject<?> value) {
        var obj = this.localVariables[id];
        if (obj != null) {
            obj.set(value);
            return;
        }

        throw new RuntimeException(null + " isn't defined in this scope!");
    }

    public XObject<?> getVariable(int id) {
        return this.localVariables[id].object;
    }

    public XObject<?> getConstant(int id) {
        return this.constants[id];
    }

    public void storeState() {
        var state = new ScopeVariable[this.localVariables.length];
        System.arraycopy(this.localVariables, 0, state, 0, state.length);
        ++this.storedStateId;
        if (this.storedVariables.length == this.storedStateId) {
            var old = this.storedVariables;
            this.storedVariables = new ScopeVariable[old.length + 8][];
            System.arraycopy(old, 0, this.storedVariables, 0, old.length);
        }
        this.storedVariables[this.storedStateId] = state;
    }

    public void restoreState() {
        System.arraycopy(this.storedVariables[this.storedStateId], 0, this.localVariables, 0, this.storedVariables[this.storedStateId].length);
    }

    public void restoreAndRemoveState() {
        System.arraycopy(this.storedVariables[this.storedStateId], 0, this.localVariables, 0, this.storedVariables[this.storedStateId].length);
        this.storedVariables[this.storedStateId] = null;
        this.storedStateId--;
    }

    @Override
    public @Nullable ScopeVariable[] asJava() {
        return this.localVariables;
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        try {
            return this.runtime.getGlobal(key);
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
        SCRIPT, LOCAL
    }

    public static class ScopeVariable {
        public final ObjectScope scope;
        public final boolean readonly;
        public final String name;
        protected XObject<?> object;

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
