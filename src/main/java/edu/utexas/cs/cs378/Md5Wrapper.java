package edu.utexas.cs.cs378;

import java.nio.ByteBuffer;

// i really don't like this because it's expensive in memory, but we don't have
// much of a choice; all the other options are either worse or requiring implementing
// the collections (to my knowledge)
public class Md5Wrapper {

    private final long upper;
    private final long lower;

    public Md5Wrapper(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("MD5 hash must be 16 bytes");
        }

        ByteBuffer buffer = ByteBuffer.wrap(array);
        this.upper = buffer.getLong();
        this.lower = buffer.getLong();
    }

    public byte[] toArray() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(upper);
        buffer.putLong(lower);
        return buffer.array();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(upper) ^ Long.hashCode(lower);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Md5Wrapper that = (Md5Wrapper) o;
        return upper == that.upper && lower == that.lower;
    }
}
