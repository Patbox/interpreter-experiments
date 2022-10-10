package eu.pb4.lang.expression;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.object.MapObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.StringMapObject;
import eu.pb4.lang.object.XObject;

public record MapExpression(Expression[][] expressions, Position info) implements Expression{
    @Override
    public XObject<?> execute(ObjectScope scope) throws InvalidOperationException {
        var list = new MapObject();
        for (int i = 0; i < expressions.length; i++) {
            list.asJava().put(expressions[i][0].execute(scope), expressions[i][1].execute(scope));
        }
        return list;
    }
}
