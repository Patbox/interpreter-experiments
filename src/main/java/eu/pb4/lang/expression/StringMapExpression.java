package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.ListObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.StringMapObject;
import eu.pb4.lang.object.XObject;

public record StringMapExpression(Expression[][] expressions, Position info) implements Expression{
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var list = new StringMapObject();
        for (int i = 0; i < expressions.length; i++) {
            list.asJava().put(expressions[i][0].execute(scope).asString(), expressions[i][1].execute(scope));
        }
        return list;
    }
}
