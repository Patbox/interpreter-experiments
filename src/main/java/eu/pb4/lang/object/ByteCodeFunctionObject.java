package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ByteArrayReader;
import eu.pb4.lang.runtime.ObjectScope;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ByteCodeFunctionObject extends XObject<ByteCodeFunctionObject> {
    private final byte[] bytecode;
    private final ObjectScope scope;
    private final int[] argsIds;

    public ByteCodeFunctionObject(ObjectScope scope, int[] argsIds, byte[] bytecode) {
        this.bytecode = bytecode;
        this.argsIds = argsIds;
        this.scope = scope;
    }

    @Override
    public String asString() { return "<function>"; }
    public String type() { return "function"; }

    @Override
    public @Nullable ByteCodeFunctionObject asJava() {
        return this;
    }

    @Override
    public XObject<?> call(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        var funcScope = new ObjectScope(this.scope);
        for (var i = 0; i < args.length; i++) {
            funcScope.declareVariable("", this.argsIds[i], args[i], false);
        }

        try {
            return scope.getRuntime().execute(new ByteArrayReader(this.bytecode), funcScope).object();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ByteCodeFunctionObject that = (ByteCodeFunctionObject) o;
        return Arrays.equals(this.bytecode, that.bytecode) && Arrays.equals(argsIds, that.argsIds) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, this.bytecode, argsIds);
    }
}
