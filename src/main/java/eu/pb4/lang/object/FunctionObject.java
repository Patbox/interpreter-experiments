package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FunctionObject extends XObject<FunctionObject> {
    private final Expression[] expressions;
    private final String[] args;
    private final ObjectScope scope;
    private final int[] argsIds;

    public FunctionObject(ObjectScope scope, List<String> args, int[] argsIds, List<Expression> expressions) {
        this.expressions = expressions.toArray(new Expression[0]);
        this.argsIds = argsIds;
        this.args = args.toArray(new String[0]);
        this.scope = scope;
    }

    @Override
    public String asString() { return "<function>"; }
    public String type() { return "function"; }

    @Override
    public @Nullable FunctionObject asJava() {
        return this;
    }

    @Override
    public XObject<?> call(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        var funcScope = new ObjectScope(this.scope);
        var count = Math.min(args.length, this.args.length);

        for (var i = 0; i < count; i++) {
            funcScope.declareVariable(this.args[i], this.argsIds[i], args[i], false);
        }

        XObject<?> lastObject = XObject.NULL;
        for (int i = 0; i < this.expressions.length; i++) {
            var expr = this.expressions[i];
            lastObject = expr.execute(funcScope);
            if (lastObject instanceof ForceReturnObject forceReturnObject) {
                return forceReturnObject.asJava();
            }
        }
        return lastObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FunctionObject that = (FunctionObject) o;
        return Arrays.equals(expressions, that.expressions) && Arrays.equals(args, that.args) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, expressions, args);
    }
}
