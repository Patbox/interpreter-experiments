package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ClassObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.util.Pair;

import java.util.List;

public record CreateClassExpression(String name, String superClass, FunctionExpression constructor,
                                    List<Pair<Pair<String, Boolean>, Expression>> fieldConstructor,
                                    List<Pair<Pair<String, Boolean>, Expression>> staticFieldConstructor,
                                    boolean isFinalClass, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        ClassObject superClass = null;

        if (this.superClass != null) {
            try {
                var obj = scope.getVariable(this.superClass);
                if (obj instanceof ClassObject classObject) {
                    superClass = classObject;

                    if (superClass.finalClass) {
                        thr(this.superClass + " is a final/non-extendable class!");
                    }
                } else {
                    thr(this.superClass + " isn't a class!");
                }
            } catch (Throwable e) {
                thr(e.getMessage());
            }
        }

        return new ClassObject(scope, this.name, superClass, this.constructor, this.fieldConstructor, this.staticFieldConstructor, isFinalClass);
    }
}
