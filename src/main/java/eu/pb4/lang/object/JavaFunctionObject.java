package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import org.jetbrains.annotations.Nullable;


public class JavaFunctionObject extends XObject<JavaFunctionObject.Function> {
    private final Function function;

    public JavaFunctionObject(Function function) {
        this.function = function;
    }

    public static JavaFunctionObject ofVoid(Consumer func) {
        return new JavaFunctionObject((a, args, i) -> {
            func.accept(a, args, i);
            return XObject.NULL;
        });
    }

    @Override
    public String asString() {
        return "<runtime function>";
    }
    public String type() {
        return "runtime function";
    }

    @Override
    public @Nullable Function asJava() {
        return this.function;
    }

    @Override
    public XObject<?> call(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        return this.function.apply(scope, args, info);
    }

    public interface Consumer {
        void accept(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException ;
    }
    public interface Function {
        XObject<?> apply(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException;
    }
}
