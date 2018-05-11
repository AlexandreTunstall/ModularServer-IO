package atunstall.server.io.api;

import java.util.function.Consumer;

/**
 * Models an object that can receive binary data.
 */
public interface OutputStream extends Consumer<ByteBuffer>, AutoCloseable {
    default Consumer<ParsableByteBuffer> toParsableBufferConsumer() {
        return b -> {
            try {
                accept(b);
            } catch (RuntimeException ignored) {}
            b.consumeAll();
        };
    }
}
