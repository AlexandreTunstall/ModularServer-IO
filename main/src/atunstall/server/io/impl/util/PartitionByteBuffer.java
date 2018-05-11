package atunstall.server.io.impl.util;

import atunstall.server.io.api.ByteBuffer;

import java.util.function.Consumer;

public class PartitionByteBuffer implements ByteBuffer {
    private final ByteBuffer parent;
    private final long index;
    private final long count;

    PartitionByteBuffer(ByteBuffer parent, long index, long count) {
        this.parent = parent;
        this.index = index;
        this.count = count;
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public byte get(long index) {
        validateArgs(index, 1L);
        return parent.get(this.index + index);
    }

    @Override
    public void get(long index, byte[] bytes, int offset, int count) {
        validateArgs(index, count);
        parent.get(this.index + index, bytes, offset, count);
    }

    @Override
    public void apply(long index, long count, Consumer<byte[]> consumer) {
        validateArgs(index, count);
        parent.apply(this.index + index, count, consumer);
    }

    @Override
    public ByteBuffer partition(long index, long count) {
        validateArgs(index, count);
        return new PartitionByteBuffer(parent, this.index + index, count);
    }

    @Override
    public void clear(long index, long count) {
        validateArgs(index, count);
        parent.clear(this.index + index, count);
    }

    private void validateArgs(long index, long count) {
        if (index + count > this.count || index < 0L) {
            throw new IllegalArgumentException("parameters are out of range");
        }
    }
}
