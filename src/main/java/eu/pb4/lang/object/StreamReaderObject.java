package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;

import java.io.*;

public class StreamReaderObject extends ReaderObject {
    public final InputStream stream;

    private JavaFunctionObject functionClose = JavaFunctionObject.ofVoid((a, b, c) -> StreamReaderObject.this.stream.close());

    public StreamReaderObject(InputStream stream) {
        super(new DataInputStream(stream));
        this.stream = stream;
    }


    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "close" -> functionClose;
            default -> super.get(scope, key, info);
        };
    }
}
