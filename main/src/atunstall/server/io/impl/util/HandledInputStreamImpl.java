package atunstall.server.io.impl.util;

import atunstall.server.io.api.ParsableByteBuffer;
import atunstall.server.io.api.util.AppendableParsableByteBuffer;
import atunstall.server.io.api.util.HandledInputStream;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class HandledInputStreamImpl implements HandledInputStream {
    private final AutoCloseable closeCallback;
    private Deque<Consumer<? super ParsableByteBuffer>> consumers;
    private boolean closed;

    protected HandledInputStreamImpl(AutoCloseable closeCallback) {
        this.closeCallback = closeCallback;
        consumers = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void queueConsumer(Consumer<? super ParsableByteBuffer> consumer) {
        consumers.add(consumer);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws Exception {
        closed = true;
        closeCallback.close();
    }

    @Override
    public void consume(ParsableByteBuffer buffer) {
        if (buffer.count() == 0L) return;
        Consumer<? super ParsableByteBuffer> consumer;
        while ((consumer = consumers.peek()) != null) {
            consumer.accept(buffer);
            if (buffer.bytesConsumed() > 0L) {
                break;
            }
            consumers.remove();
        }
    }

    @Override
    public void consumeSafe(AppendableParsableByteBuffer buffer) {
        while (buffer.count() > 0L) {
            try {
                consume(buffer);
                buffer.updateBackup();
            } catch (RuntimeException e) {
                System.out.println("[OI] Consumer threw exception, rolling back buffer");
                e.printStackTrace();
                buffer.rollback();
                if (closed && consumers.size() > 0) {
                    System.out.println("Stream is closed, dropping consumer and retrying");
                    consumers.remove();
                    continue;
                }
                break;
            }
        }
    }

    @Override
    public int consumerCount() {
        return consumers.size();
    }
}
