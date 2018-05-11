package atunstall.server.io.impl.util;

import atunstall.server.io.api.OutputStream;
import atunstall.server.io.api.util.AppendableParsableByteBuffer;

public class WrappedOutputStream extends java.io.OutputStream {
    private static final int BUFFER_SIZE = 4096;

    private final OutputStream stream;
    private final AppendableParsableByteBuffer buffer;
    private final byte[] rawBuffer;
    private int index;

    WrappedOutputStream(OutputStream stream) {
        this.stream = stream;
        buffer = new ArrayAppendableParsableByteBuffer(BUFFER_SIZE);
        rawBuffer = new byte[BUFFER_SIZE];
        index = 0;
    }

    @Override
    public void write(int b) {
        synchronized (buffer) {
            if (index >= rawBuffer.length) flush();
            rawBuffer[index++] = (byte) b;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        synchronized (buffer) {
            buffer.append(b, off, len);
            if (buffer.count() > BUFFER_SIZE) flush();
        }
    }

    @Override
    public void flush() {
        synchronized (buffer) {
            buffer.append(rawBuffer, 0, index);
            stream.toParsableBufferConsumer().accept(buffer);
            index = 0;
        }
    }
}
