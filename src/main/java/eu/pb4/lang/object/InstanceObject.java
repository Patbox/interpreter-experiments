package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InstanceObject extends XObject<Map<String, XObject<?>>> {
    private final Map<String, XObject<?>> variables = new HashMap<>();
    private final Map<String, Boolean> variablesReadOnly = new HashMap<>();
    private final ClassObject classObject;
    private final Map<ClassObject, Map<String, XObject<?>>> variablesByClass = new HashMap<>();

    public InstanceObject(ClassObject classObject) {
        this.classObject = classObject;
    }

    @Override
    public String asString() {
        return "{ object " + this.classObject.name() + "} ";
    }

    @Override
    public String type() {
        return "object " + this.classObject.name();
    }

    public void declareField(ClassObject classObject, String name, XObject<?> value, boolean readOnly, Expression.Position info) throws InvalidOperationException {
        this.variables.put(name, value);
        if (classObject != null) {
            this.variablesByClass.computeIfAbsent(classObject, (x) -> new HashMap<>()).put(name, value);
        }
        this.variablesReadOnly.put(name, readOnly);
    }

    public void setVariable(String name, XObject<?> value, Expression.Position info) throws InvalidOperationException {
        if (this.variables.containsKey(name)) {
            if (this.variablesReadOnly.get(name) == Boolean.TRUE) {
                throw new InvalidOperationException(info, name + " is readonly!");
            }

            this.variables.put(name, value);
        } else {
            throw new InvalidOperationException(info, name + " isn't defined in this object!");
        }

    }

    public XObject<?> getVariable(String name, Expression.Position info) throws InvalidOperationException {
        if (this.variables.containsKey(name)) {
            return this.variables.get(name);
        }

        throw new InvalidOperationException(info, name + " isn't defined in this object!");
    }

    @Override
    public @Nullable Map<String, XObject<?>> asJava() {
        return this.variables;
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return this.getVariable(key, info);
    }

    @Override
    public void set(ObjectScope scope, String key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        this.setVariable(key, object, info);
    }

    public ObjectScope withScope(ObjectScope scope) {
        scope = new ObjectScope(scope);
        scope.declareVariable("this", this, true);
        return scope;
    }

    public XObject<?> getAccessor(ClassObject superClass) {
        return new AccessorObject(superClass);
    }

    public class AccessorObject extends StaticStringMapObject {
        private final ClassObject classObject;

        public AccessorObject(ClassObject classObject) {
            super(InstanceObject.this.variablesByClass.getOrDefault(classObject, Map.of()));
            this.classObject = classObject;
        }

        @Override
        public XObject<?> call(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
            this.classObject.callConstructor(InstanceObject.this, args, info);
            return XObject.NULL;
        }
    }
}
