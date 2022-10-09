package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public record ExportExpression(String key, Expression value) implements Expression {

    @Override
    public XObject<?> execute(ObjectScope scope) {
        scope.addExport(key, value.execute(scope));
        return XObject.NULL;
    }
}
