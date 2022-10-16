package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class IteratorObject extends XObject<Iterator<XObject<?>>> {
    private final Iterator<XObject<?>> iterator;

    public IteratorObject(Iterator<XObject<?>> iterator) {
        this.iterator = iterator;
    }


    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "hasNext" -> BooleanObject.of(this.iterator.hasNext());
            case "next" -> this.iterator.next();
            default -> super.get(scope, key, info);
        };
    }

    @Override
    public @Nullable Iterator<XObject<?>> asJava() {
        return null;
    }

    @Override
    public String asString() {
        return null;
    }
}
