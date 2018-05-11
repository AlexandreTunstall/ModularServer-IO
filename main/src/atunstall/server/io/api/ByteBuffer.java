package atunstall.server.io.api;

import java.util.function.Consumer;

/**
 * A non-parsable buffer.
 * Used in situations where the buffer does not need to be parsable.
 */
public interface ByteBuffer {
    /**
     * The number of bytes currently inside this buffer.
     * @return The number of bytes as a long.
     */
    long count();

    /**
     * Reads the byte at the given index from this buffer.
     * @param index The index of the byte to read.
     * @return The byte.
     * @throws RuntimeException If the buffer is too small to contain a byte at the given index.
     */
    byte get(long index);

    /**
     * Reads the bytes at the given index from this buffer and stores them inside the given array.
     * @param index The index of the bytes to read.
     * @param bytes The array in which to store the read bytes.
     * @param offset The array offset at which to store the bytes.
     * @param count The number of bytes to read.
     * @throws RuntimeException If the buffer is too small to contain the requested number of bytes at the given index.
     */
    void get(long index, byte[] bytes, int offset, int count);

    /**
     * Copies the bytes in order into the consumer.
     * The consumer may be called several times if necessary.
     * @param index The index of the first byte to send to the consumer.
     * @param count The number of bytes to send to the consumer.
     * @param consumer The consumer to which to send the data.
     */
    void apply(long index, long count, Consumer<byte[]> consumer);

    /**
     * Returns a partition of this buffer.
     * If the requested partition doesn't already exist, it is created.
     * Changes made to this buffer or the created partition are reflected between both buffers.
     * @param index The index of the first byte in the partition.
     * @param count The number of bytes to add to the partition.
     * @return The partition that reflects the given index range.
     */
    ByteBuffer partition(long index, long count);

    /**
     * Fills the buffer with useless values.
     * This method exists for security purposes to allow users to remove sensitive information from memory.
     * @param index The index of the first byte to clear.
     * @param count The number of bytes to clear from this buffer.
     */
    void clear(long index, long count);
}
