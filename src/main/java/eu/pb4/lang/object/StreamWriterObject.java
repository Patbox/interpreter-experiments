package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;

import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.OutputStream;

public class StreamWriterObject extends WriterObject {
    public final OutputStream stream;

    private JavaFunctionObject functionClose = JavaFunctionObject.ofVoid((a, b, c) -> StreamWriterObject.this.stream.close());

    public StreamWriterObject(OutputStream stream) {
        super(new DataOutputStream(stream));
        this.stream = stream;

        if (stream instanceof Flushable) {

        }
    }


    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "close" -> functionClose;
            default -> super.get(scope, key, info);
        };
    }
}
