package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.InstanceObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record GetClassExpression(Expression expression, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        if (expression.execute(scope) instanceof InstanceObject instanceObject) {
            return instanceObject.classObject;
        }

        return thr( "object isn't an instance of a class");
    }
}
