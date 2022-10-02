package eu.pb4.lang.expression;

import eu.pb4.lang.object.ObjectScope;
import eu.pb4.lang.object.XObject;

public interface Expression {
    Expression NOOP = (x) -> XObject.NULL;

    XObject<?> execute(ObjectScope scope);
}
