package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class ByteArrayWriterObject extends WriterObject {
    private final ByteArrayOutputStream stream;

    public ByteArrayWriterObject() {
        this(new ByteArrayOutputStream());
    }

    public ByteArrayWriterObject(ByteArrayOutputStream stream) {
        super(new DataOutputStream(stream));
        this.stream = stream;
    }


    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "buffer" -> new ByteArrayObject(this.stream.toByteArray());
            default -> super.get(scope, key, info);
        };
    }

    public static XObject<?> create(ObjectScope scope, XObject<?>[] xObjects, Expression.Position position) {
        return new ByteArrayWriterObject();
    }
}
