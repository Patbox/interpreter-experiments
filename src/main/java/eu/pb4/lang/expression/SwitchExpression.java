package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ForceReturnObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;
import eu.pb4.lang.util.Pair;

import java.util.List;

public record SwitchExpression(Expression value, List<Pair<Expression, List<Expression>>> expressions, List<Expression> defaultExpr, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var value = this.value.execute(scope);
        var executable = defaultExpr;

        for (var pair : this.expressions) {
            if (pair.left().execute(scope).equalsObj(scope, value, pair.left().info())) {
                executable = pair.right();
                break;
            }
        }

        XObject<?> lastObject = XObject.NULL;
        var subScope = new ObjectScope(scope);
        for (var e : executable) {
            lastObject = e.execute(subScope);
            if (lastObject instanceof ForceReturnObject x) {
                return x.type == ForceReturnObject.Type.SWITCH ? x.asJava() : lastObject;
            }
        }
        return lastObject;
    }
}
