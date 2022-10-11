package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

public class ForceReturnObject extends XObject<XObject<?>>{
    private final XObject<?> object;
    public final Type type;

    public ForceReturnObject(XObject<?> object, Type type) {
        while (object instanceof ForceReturnObject x) {
            object = x.asJava();
        }

        this.object = object;
        this.type = type;
    }

    @Override
    public @Nullable XObject<?> asJava() {
        return this.object;
    }

    @Override
    public String asString() {
        return "<BAD>";
    }

    public enum Type {
        FULL,
        SWITCH
    }
}
