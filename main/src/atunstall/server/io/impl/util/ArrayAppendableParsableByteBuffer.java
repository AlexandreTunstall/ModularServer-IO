package atunstall.server.io.impl.util;

import atunstall.server.io.api.ByteBuffer;
import atunstall.server.io.api.util.AppendableParsableByteBuffer;

// TODO Allow more than Integer.MAX_VALUE bytes.
public class ArrayAppendableParsableByteBuffer extends ArrayParsableByteBuffer implements AppendableParsableByteBuffer {
    private byte[] copy;
    private int copyIndex;
    private int copyLength;

    ArrayAppendableParsableByteBuffer(int length) {
        super(new byte[length]);
        copy = new byte[length];
        this.length = 0;
        updateBackup();
    }

    @Override
    public void append(byte[] bytes, int offset, int count) {
        validateSize(count);
        System.arraycopy(bytes, offset, buffer, index + length, count);
        length += count;
        updateBackup();
    }

    @Override
    public void append(ByteBuffer buffer) {
        validateSize(buffer.count());
        buffer.get(0L, this.buffer, index + length, (int) buffer.count());
    }

    @Override
    public void rollback() {
        System.arraycopy(copy, copyIndex, buffer, index = copyIndex, length = copyLength);
        reset();
    }

    @Override
    public void updateBackup() {
        System.arraycopy(buffer, index, copy, copyIndex = index, copyLength = length);
        reset();
    }

    private void validateSize(long count) {
        if (index + length + count > buffer.length) {
            if (index > buffer.length / 2 && count + length < buffer.length) {
                System.arraycopy(buffer, index, buffer, 0, length);
            } else {
                int newLength = Math.min(buffer.length, 256);
                while (index + length + count > (newLength <<= 1)) ;     // Intentionally empty loop
                byte[] newBuffer = new byte[newLength];
                System.arraycopy(buffer, index, newBuffer, 0, length);
                buffer = newBuffer;
                copy = new byte[newBuffer.length];
            }
            index = 0;
        }
    }
}
