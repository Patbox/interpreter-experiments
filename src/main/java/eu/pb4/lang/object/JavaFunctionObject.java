package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class JavaFunctionObject extends XObject<Function<XObject<?>[],XObject<?>>> {
    private final Function<XObject<?>[], XObject<?>> function;

    public JavaFunctionObject(Function<XObject<?>[], XObject<?>> function) {
        this.function = function;
    }

    public static JavaFunctionObject ofVoid(Consumer<XObject<?>[]> func) {
        return new JavaFunctionObject((args) -> {
            func.accept(args);
            return XObject.NULL;
        });
    }

    @Override
    public String asString() {
        return "<runtime function>";
    }

    @Override
    public @Nullable Function<XObject<?>[], XObject<?>> asJava() {
        return this.function;
    }

    @Override
    public XObject<?> call(XObject<?>... args) {
        return this.function.apply(args);
    }
}
