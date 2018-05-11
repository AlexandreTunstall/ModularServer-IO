package atunstall.server.io.impl.util;

import atunstall.server.io.api.InputStream;
import atunstall.server.io.api.ParsableByteBuffer;
import atunstall.server.io.api.util.AppendableParsableByteBuffer;

public class WrappedInputStream extends java.io.InputStream {
    private final AppendableParsableByteBuffer buffer;

    WrappedInputStream(InputStream stream) {
        buffer = new ArrayAppendableParsableByteBuffer(4096);
        stream.queueConsumer(this::read);
    }

    @Override
    public int read() {
        int value;
        synchronized (buffer) {
            while (buffer.count() <= 0L) {
                try {
                    buffer.wait();
                } catch (InterruptedException ignored) {}
            }
            value = buffer.get(0L);
            buffer.consume(0L, 1L);
        }
        return value;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        synchronized (buffer) {
            while (buffer.count() <= 0L) {
                try {
                    buffer.wait();
                } catch (InterruptedException ignored) {}
            }
            len = (int) Math.min(len, buffer.count());
            buffer.get(0L, b, off, len);
            buffer.consume(0L, len);
            return len;
        }
    }

    private void read(ParsableByteBuffer buffer) {
        synchronized (this.buffer) {
            this.buffer.append(buffer);
            this.buffer.notifyAll();
        }
        buffer.consumeAll();
    }
}
