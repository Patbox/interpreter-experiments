package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ListObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record ListExpression(Expression[] expressions, Position info) implements Expression{
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var list = new ListObject();
        for (int i = 0; i < expressions.length; i++) {
            list.asJava().add(expressions[i].execute(scope));
        }
        return list;
    }
}
