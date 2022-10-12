package eu.pb4.lang.libs;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.object.*;
import eu.pb4.lang.util.GenUtils;
import eu.pb4.lang.util.ObjectBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileSystemLibrary {
    public static XObject<?> build() {
        return new ObjectBuilder()
                .varArg("readString", FileSystemLibrary::readString)
                .varArg("read", FileSystemLibrary::read)
                .varArg("reader", FileSystemLibrary::reader)
                .varArg("write", FileSystemLibrary::write)
                .varArg("writer", FileSystemLibrary::writer)
                .varArg("mkdir", FileSystemLibrary::mkdir)
                .varArg("existsFile", FileSystemLibrary::existFile)
                .varArg("existsDir", FileSystemLibrary::existDir)
                .varArg("exists", FileSystemLibrary::exist)
                .varArg("path", FileSystemLibrary::path)
                .build();
    }

    private static XObject<?> writer(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        try {
            return new StreamWriterObject(Files.newOutputStream(Path.of(args[0].asString())));
        } catch (IOException e) {
            return XObject.NULL;
        }
    }

    private static XObject<?> reader(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        try {
            return new StreamReaderObject(Files.newInputStream(Path.of(args[0].asString())));
        } catch (IOException e) {
            return XObject.NULL;
        }
    }

    private static XObject<?> existDir(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        var path = Path.of(args[0].asString());
        return BooleanObject.of(Files.isDirectory(path));
    }

    private static XObject<?> existFile(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        var path = Path.of(args[0].asString());
        return BooleanObject.of(Files.isRegularFile(path));
    }

    private static XObject<?> exist(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        var path = Path.of(args[0].asString());

        return BooleanObject.of(Files.exists(path));
    }

    private static XObject<?> path(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        var path = Path.of(args.length != 0 ? args[0].asString() : "./");
        return new StringObject(path.toAbsolutePath().normalize().toString());
    }

    private static XObject<?> mkdir(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        var path = Path.of(args[0].asString());

        try {
            Files.createDirectories(path);
            return BooleanObject.TRUE;
        } catch (IOException e) {
            return BooleanObject.FALSE;
        }
    }

    private static XObject<?> readString(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        var path = Path.of(args[0].asString());

        try {
            return new StringObject(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return XObject.NULL;
        }
    }

    private static XObject<?> write(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 2, info);

        try {
            Files.write(Path.of(args[0].asString()), args[1].asBytes(info));
            return BooleanObject.TRUE;
        } catch (IOException e) {
            return BooleanObject.FALSE;
        }
    }

    private static XObject<?> read(ObjectScope scope, XObject<?>[] args, Expression.Position info) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, info);

        try {
            return new ByteArrayObject(Files.readAllBytes(Path.of(args[0].asString())));
        } catch (IOException e) {
            return XObject.NULL;
        }
    }
}
