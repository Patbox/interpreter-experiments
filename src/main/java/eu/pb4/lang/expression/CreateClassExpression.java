package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ClassObject;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.runtime.StaticObjectConsumer;
import eu.pb4.lang.util.Pair;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public record CreateClassExpression(String name, String superClass, int superClassId, FunctionExpression constructor,
                                    List<Pair<Pair<String, Boolean>, Expression>> fieldConstructor,
                                    List<Pair<Pair<String, Boolean>, Expression>> staticFieldConstructor,
                                    boolean isFinalClass, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        ClassObject superClass = null;

        if (this.superClass != null) {
            try {
                var obj = this.superClassId != -1 ? scope.getVariable(this.superClassId) : scope.getRuntime().getGlobal(this.superClass);
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

    @Override
    public void writeByteCode(DataOutputStream output, StaticObjectConsumer objects) throws IOException {

    }
}
