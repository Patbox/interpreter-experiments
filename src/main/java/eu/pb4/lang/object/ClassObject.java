package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.expression.FunctionExpression;
import eu.pb4.lang.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClassObject extends XObject<String> {
    private final String name;
    private final ClassObject superClass;
    @Nullable
    private final FunctionExpression constructor;
    private final List<Pair<Pair<String, Boolean>, Expression>> fieldConstructor;
    private final ObjectScope scope;
    private final ArrayList<Pair<Pair<String, Boolean>, Expression>> methodConstructor;
    public final boolean finalClass;

    public ClassObject(ObjectScope scope, String name, ClassObject superClass, FunctionExpression constructor,
                       List<Pair<Pair<String, Boolean>, Expression>> fieldConstructor,
                       List<Pair<Pair<String, Boolean>, Expression>> staticFieldConstructor, boolean isFinalClass) throws InvalidOperationException {
        this.scope = new ObjectScope(scope);
        this.name = name;
        this.superClass = superClass;
        this.constructor = constructor;
        this.finalClass = isFinalClass;
        this.fieldConstructor = new ArrayList<>();
        this.methodConstructor = new ArrayList<>();

        for (var field : fieldConstructor) {
            (field.right() instanceof FunctionExpression ? this.methodConstructor : this.fieldConstructor).add(field);
        }

        for (var field : staticFieldConstructor) {
            this.scope.declareVariable(field.left().left(), field.right().execute(scope), field.left().right());
        }
    }

    @Override
    public @Nullable String asJava() {
        return this.name;
    }

    @Override
    public XObject<?> call(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        var object = new InstanceObject(this);
        this.populateMethods(object, info);
        this.populateFields(object, info);
        this.callConstructor(object, args, info);

        return object;
    }

    public void callConstructor(InstanceObject object, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        var objectScope = object.withScope(this.scope);
        if (this.constructor != null) {
            if (this.superClass != null) {
                scope.declareVariable("super", object.getAccessor(this.superClass), true);
            }
            this.constructor.execute(objectScope).call(scope, args, info);
        } else if (this.superClass != null) {
            this.superClass.callConstructor(object, args, info);
        }
    }


    private void populateMethods(InstanceObject object, Expression.Position info) throws InvalidOperationException {
        if (this.superClass != null) {
            this.superClass.populateMethods(object, info);
        }

        var scope = object.withScope(this.scope);

        for (var field : methodConstructor) {
            object.declareField( this, field.left().left(), field.right().execute(scope), field.left().right(), info);
        }
    }

    private void populateFields(InstanceObject object, Expression.Position info) throws InvalidOperationException {
        if (this.superClass != null) {
            this.superClass.populateFields(object, info);
        }

        var scope = object.withScope(this.scope);

        for (var field : fieldConstructor) {
            object.declareField(null, field.left().left(), field.right().execute(scope), field.left().right(), info);
        }
    }



    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return this.scope.get(scope, key, info);
    }

    @Override
    public void set(ObjectScope scope, String key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        this.scope.set(scope, key, object, info);
    }

    @Override
    public String asString() {
        return "<class " + this.name + ">";
    }

    @Override
    public String type() {
        return "class";
    }

    public String name() {
        return this.name;
    }
}
