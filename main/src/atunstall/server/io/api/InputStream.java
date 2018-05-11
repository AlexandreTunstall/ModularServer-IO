package atunstall.server.io.api;

import java.util.function.Consumer;

/**
 * Models an object capable of outputting binary data.
 */
public interface InputStream extends AutoCloseable {
    /**
     * Adds a consumer to the FIFO queue of consumers that this stream should send the read buffer to.
     * The consumer must throw an exception to indicate that it does not have enough data.
     * If a consumer throws an exception when passed a buffer, the buffer will be restored to before it was consumed.
     * If a consumer throws an exception, it may not be called again until the buffer has received additional data.
     * If a consumer throws an exception and the stream has been closed, the consumer will be removed from the queue and the next consumer will be used.
     * If a consumer returns without consuming any data, the consumer will be removed from the queue and the next consumer will be used.
     * Once this stream has been closed, it will continue sending the buffer to the consumers until the buffer is empty.
     * If this stream is closed and the buffer is empty, this method will do nothing.
     * @param consumer The consumer to set this to.
     */
    void queueConsumer(Consumer<? super ParsableByteBuffer> consumer);

    /**
     * Checks if this stream is closed.
     * Streams with finite length will automatically close once the end of file has been reached.
     * @return True if this stream is closed, false otherwise.
     */
    boolean isClosed();
}
