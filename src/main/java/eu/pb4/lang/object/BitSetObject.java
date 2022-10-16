package eu.pb4.lang.object;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.runtime.ObjectScope;
import eu.pb4.lang.util.GenUtils;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public class BitSetObject extends XObject<BitSet> {
    private final BitSet bitSet;

    public BitSetObject(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    public static XObject<?> create(ObjectScope scope, XObject<?>[] args, Expression.Position position) throws InvalidOperationException {
        GenUtils.argumentCount(args, 1, position);

        BitSet set;

        if (args[0] instanceof ByteArrayObject bufferObject) {
            set = BitSet.valueOf(bufferObject.asBytes(position));
        } else {
            set = new BitSet(args[0].asInt(position));
        }

        return new BitSetObject(set);

    }

    @Override
    public @Nullable BitSet asJava() {
        return this.bitSet;
    }

    @Override
    public String asString() {
        return "<bitset>";
    }

    @Override
    public String type() {
        return "bitset";
    }

    @Override
    public void set(ObjectScope scope, XObject<?> key, XObject<?> object, Expression.Position info) throws InvalidOperationException {
        var val = key.asInt(info);

        if (val >= this.bitSet.size()) {
            throw new InvalidOperationException(info, "tried to access index " + val + " out of " + this.bitSet.size());
        } else if (val < 0) {
            throw new InvalidOperationException(info, "tried to access index below zero -> " + val);
        }

        bitSet.set(val, object.asBoolean(info));
    }

    @Override
    public XObject<?> get(ObjectScope scope, XObject<?> key, Expression.Position info) throws InvalidOperationException {
        var val = key.asInt(info);

        if (val >= this.bitSet.size()) {
            throw new InvalidOperationException(info, "tried to access index " + val + " out of " + this.bitSet.size());
        } else if (val < 0) {
            throw new InvalidOperationException(info, "tried to access index below zero -> " + val);
        }

        return BooleanObject.of(this.bitSet.get(val));
    }

    @Override
    public XObject<?> get(ObjectScope scope, String key, Expression.Position info) throws InvalidOperationException {
        return switch (key) {
            case "length" -> NumberObject.of(this.bitSet.length());
            default -> super.get(scope, key, info);
        };
    }
}
