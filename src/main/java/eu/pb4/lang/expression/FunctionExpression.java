package eu.pb4.lang.expression;

import eu.pb4.lang.object.FunctionObject;
import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

import java.util.List;

public record FunctionExpression(List<String> args, List<Expression> expressions, Position info) implements Expression {
    @Override
    public XObject<?> execute(ObjectScope scope) {
        return new FunctionObject(scope, args, expressions);
    }
}
