package atunstall.server.io.impl.util;

import atunstall.server.io.api.ByteBuffer;

import java.util.Arrays;
import java.util.function.Consumer;

public class ArrayByteBuffer implements ByteBuffer {
    private final byte[] buffer;

    ArrayByteBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    @Override
    public long count() {
        return buffer.length;
    }

    @Override
    public byte get(long index) {
        validateArgs(index, 1L);
        return buffer[(int) index];
    }

    @Override
    public void get(long index, byte[] bytes, int offset, int count) {
        validateArgs(index, count);
        System.arraycopy(buffer, (int) index, bytes, offset, count);
    }

    @Override
    public void apply(long index, long count, Consumer<byte[]> consumer) {
        validateArgs(index, count);
        int intCount = (int) count;
        byte[] copy = new byte[intCount];
        System.arraycopy(buffer, (int) index, copy, 0, intCount);
        consumer.accept(copy);
    }

    @Override
    public ByteBuffer partition(long index, long count) {
        validateArgs(index, count);
        return new PartitionByteBuffer(this, index, count);
    }

    @Override
    public void clear(long index, long count) {
        validateArgs(index, count);
        Arrays.fill(buffer, (int) index, (int) (index + count), (byte) 0);
    }

    private void validateArgs(long index, long count) {
        if (index + count > buffer.length) {
            throw new IllegalArgumentException("index is too large");
        } else if (index < 0) {
            throw new IllegalArgumentException("index is negative");
        }
    }
}
