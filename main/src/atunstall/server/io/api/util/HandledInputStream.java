package atunstall.server.io.api.util;

import atunstall.server.io.api.InputStream;
import atunstall.server.io.api.ParsableByteBuffer;

/**
 * A utility interface to make creating input streams easier.
 */
public interface HandledInputStream extends InputStream {
    /**
     * Passes the given buffer to the consumers.
     * @param buffer The buffer to consume.
     */
    void consume(ParsableByteBuffer buffer);

    /**
     * Does the same thing as {@link #consume(ParsableByteBuffer)}, but also takes care of catching exceptions and updating the buffer's cycle.
     * @param buffer The buffer to consume.
     */
    void consumeSafe(AppendableParsableByteBuffer buffer);

    /**
     * Returns the number of consumers queued in this stream.
     * @return The number of queued consumers.
     */
    int consumerCount();
}
