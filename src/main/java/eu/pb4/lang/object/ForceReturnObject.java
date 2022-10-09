package eu.pb4.lang.object;

import org.jetbrains.annotations.Nullable;

public class ForceReturnObject extends XObject<XObject<?>>{
    private final XObject<?> object;

    public ForceReturnObject(XObject<?> object) {
        while (object instanceof ForceReturnObject x) {
            object = x.asJava();
        }

        this.object = object;
    }

    @Override
    public @Nullable XObject<?> asJava() {
        return this.object;
    }

    @Override
    public String asString() {
        return "<BAD>";
    }
}
