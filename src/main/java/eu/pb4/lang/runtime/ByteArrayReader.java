package eu.pb4.lang.runtime;

public final class ByteArrayReader {
    private final byte[] arr;
    public int pos;

    public ByteArrayReader(byte[] bytecode) {
        this.arr = bytecode;
    }

    public boolean hasMore() {
        return this.arr.length > this.pos;
    }

    public byte readByte() {
        return this.arr[this.pos++];
    }

    public int read() {
        return this.arr[this.pos++] & 0xFF;
    }

    public void read(byte[] out) {
        System.arraycopy(this.arr, this.pos, out, 0, out.length);
        this.pos += out.length;
    }

    public int readUShort() {
        return (read() << 8) | read();
    }

    public int readInt() {
        return ((this.arr[this.pos++] & 0xFF) << 24) | ((this.arr[this.pos++] & 0xFF) << 16) | ((this.arr[this.pos++] & 0xFF) << 8) | (this.arr[this.pos++] & 0xFF);
    }

    public long readLong() {
        return (((long)this.arr[this.pos++] << 56) |
                ((this.arr[this.pos++] & 255l) << 48) |
                ((this.arr[this.pos++] & 255l) << 40) |
                ((this.arr[this.pos++] & 255l) << 32) |
                ((this.arr[this.pos++] & 255l) << 24) |
                ((this.arr[this.pos++] & 255) << 16) |
                ((this.arr[this.pos++] & 255) <<  8) |
                ((this.arr[this.pos++] & 255) <<  0));
    }

    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }
}
