package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ObjectScope extends XObject<Map<String, XObject<?>>> {

    @Nullable
    private final ObjectScope parentScope;

    private Map<String, XObject<?>> variables = new HashMap<>();
    private boolean writable = true;

    public ObjectScope(@Nullable ObjectScope parentScope) {
        this.parentScope = parentScope;
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
    public XObject<?> get(String key) {
        return this.getVariable(key);
    }

    @Override
    public String asString() {
        return "<this>";
    }
}
