package atunstall.server.io.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsable buffer used by input sources.
 */
public interface ParsableByteBuffer extends ByteBuffer {
    /**
     * Finds the first occurrence starting from the given index of the given byte sequence in this buffer.
     * @param index The starting index of the search.
     * @param sequence The sequence of bytes to look for.
     * @return The index of the occurrence of the sequence, such that {@link #get(long, byte[], int, int)} reads the given sequence from this buffer.
     * @throws RuntimeException If the buffer does not contain an occurrence of the sequence.
     */
    long find(long index, byte[] sequence);

    /**
     * Compares the bytes at the given index to the given sequence.
     * @param index The starting index of the comparison.
     * @param sequence The sequence to compare to.
     * @return True if the bytes are equal, false otherwise.
     * @throws RuntimeException If the buffer does not contain enough bytes to contain the sequence at the given index.
     */
    boolean compare(long index, byte[] sequence);

    /**
     * Consumes the given number of bytes at the given index.
     * Once consumed, the bytes are no longer stored inside this buffer and the other bytes' indexes are moved to fill the gap.
     * Tbe buffer may rollback the consumed data if the supplier's contract requires it.
     * @param index The index at which to consume the bytes.
     * @param count The number of bytes to consume.
     */
    void consume(long index, long count);

    /**
     * Consumes all the bytes in this buffer.
     * This is equivalent to {@code consume(0L, count())}.
     */
    void consumeAll();

    /**
     * Returns the number of bytes consumed in the current cycle.
     * @return The number of bytes consumed in a long.
     */
    long bytesConsumed();

    ParsableByteBuffer partition(long index, long count);

    /**
     * Creates partitions of this buffer separated by the given byte sequence.
     * The created partitions do not include the byte sequence.
     * @param splitter The byte sequence around which to split this buffer.
     * @return The partitions as a list.
     */
    default List<ParsableByteBuffer> split(byte[] splitter) {
        List<ParsableByteBuffer> result = new ArrayList<>();
        long index = 0L, last = index;
        while (true) {
            try {
                index = find(last, splitter);
            } catch (IllegalStateException e) {
                return result;
            } finally {
                result.add(partition(last, index - last));
            }
            last = index + splitter.length;
            index = count();
        }
    }

    /**
     * Creates a string from this buffer using the given character set.
     * @param charset The character set with which the buffer's data is encoded.
     * @return The created string.
     */
    String toString(long index, long count, Charset charset);
}
