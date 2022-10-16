package eu.pb4.lang.libs;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.object.*;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.util.GenUtils;
import eu.pb4.lang.util.ObjectBuilder;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPLibrary {
    public static XObject<?> build() {
        return new ObjectBuilder()
                .varArg("writer", GZIPLibrary::writer)
                .varArg("reader", GZIPLibrary::reader)
                .build();
    }

    private static XObject<?> writer(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);
        if (args[0] instanceof StreamWriterObject writer) {
            try {
                return new StreamWriterObject(new GZIPOutputStream(writer.stream));
            } catch (Throwable e) {}
        }

        throw new InvalidOperationException(info, args[0].asString() + " isn't a stream writer!");
    }

    private static XObject<?> reader(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);
        if (args[0] instanceof StreamReaderObject reader) {
            try {
                return new StreamReaderObject(new GZIPInputStream(reader.stream));
            } catch (Throwable e) {}
        }

        throw new InvalidOperationException(info, args[0].asString() + " isn't a stream reader!");
    }
}
