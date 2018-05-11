package atunstall.server.io.api.util;

import atunstall.server.core.api.Unique;
import atunstall.server.core.api.Version;
import atunstall.server.io.api.ByteBuffer;
import atunstall.server.io.api.InputStream;
import atunstall.server.io.api.OutputStream;
import atunstall.server.io.api.ParsableByteBuffer;

import java.util.List;

/**
 * Service that makes creating byte buffers from byte arrays much simpler.
 */
@Version(major = 1, minor = 0)
@Unique
public interface ArrayStreams {
    /**
     * Creates an unmodifiable byte buffer using the given byte array.
     * @param data The bytes to store inside the buffer.
     * @return The created buffer.
     */
    ByteBuffer createByteBuffer(byte[] data);

    /**
     * Creates a dynamic-size appendable parsable byte buffer.
     * @param length The size of the buffer.
     * @return The created buffer.
     */
    AppendableParsableByteBuffer createByteBuffer(int length);

    /**
     * Creates a partition of the given byte buffer.
     * {@link ByteBuffer#partition(long, long)} should be used instead if you're a consumer of the byte buffer.
     * @param buffer The buffer of which to create a partition.
     * @param index The starting index of the partition.
     * @param count The number of bytes the partition should contain.
     * @return The created partition.
     */
    ByteBuffer createPartition(ByteBuffer buffer, long index, long count);

    /**
     * Creates a parsable byte buffer that concatenates the given parsable byte buffers and updates them whenever bytes are consumed.
     * @param buffers The byte buffers to concatenate, in the concatenation order.
     * @return The created buffer.
     */
    ParsableByteBuffer concatenate(List<ParsableByteBuffer> buffers);

    /**
     * Creates an input stream that automatically handles its consumers.
     * @param closeCallback Runnable that is ran when the close() method on the created stream is called.
     * @return The created handled input stream.
     */
    HandledInputStream createInputStream(AutoCloseable closeCallback);

    /**
     * Creates an input stream that automatically handles its consumers.
     * This is equivalent to {@code createInputStream(() -> {})}
     * @return The created handled input stream.
     */
    default HandledInputStream createInputStream() {
        return createInputStream(() -> {});
    }

    /**
     * Wraps the given asynchronous input stream to a Java input stream.
     * @param stream The stream to wrap.
     * @return The wrapped stream.
     */
    java.io.InputStream wrap(InputStream stream);

    /**
     * Wraps the given asynchronous output stream to a Java output stream.
     * @param stream The stream to wrap.
     * @return The wrapped stream.
     */
    java.io.OutputStream wrap(OutputStream stream);
}
