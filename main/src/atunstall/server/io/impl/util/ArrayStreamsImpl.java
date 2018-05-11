package atunstall.server.io.impl.util;

import atunstall.server.core.api.Module;
import atunstall.server.io.api.ByteBuffer;
import atunstall.server.io.api.util.AppendableParsableByteBuffer;
import atunstall.server.io.api.util.ArrayStreams;
import atunstall.server.io.api.ParsableByteBuffer;
import atunstall.server.io.api.util.HandledInputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

@Module
public class ArrayStreamsImpl implements ArrayStreams {
    @Override
    public ByteBuffer createByteBuffer(byte[] data) {
        return new ArrayByteBuffer(Arrays.copyOf(data, data.length));
    }

    @Override
    public AppendableParsableByteBuffer createByteBuffer(int length) {
        return new ArrayAppendableParsableByteBuffer(length);
    }

    @Override
    public ByteBuffer createPartition(ByteBuffer buffer, long index, long count) {
        if (index + count > buffer.count() || index < 0L) {
            throw new IllegalArgumentException("partition arguments are out of range");
        }
        return new PartitionByteBuffer(buffer, index, count);
    }

    @Override
    public ParsableByteBuffer concatenate(List<ParsableByteBuffer> buffers) {
        switch (buffers.size()) {
            case 0:
                return createByteBuffer(0);
            case 1:
                return createByteBuffer(256);
            default:
                return new ConcatenatedParsableByteBuffer(this, buffers);
        }
    }

    @Override
    public HandledInputStream createInputStream(AutoCloseable closeCallback) {
        return new HandledInputStreamImpl(closeCallback);
    }

    @Override
    public InputStream wrap(atunstall.server.io.api.InputStream stream) {
        return new WrappedInputStream(stream);
    }

    @Override
    public OutputStream wrap(atunstall.server.io.api.OutputStream stream) {
        return new WrappedOutputStream(stream);
    }
}
