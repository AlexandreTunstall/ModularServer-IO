package atunstall.server.io.api.util;

import atunstall.server.io.api.ByteBuffer;
import atunstall.server.io.api.ParsableByteBuffer;

/**
 * Models a parsable byte buffer that can be appended to.
 */
public interface AppendableParsableByteBuffer extends ParsableByteBuffer {
    /**
     * Appends the given bytes to this byte buffer.
     * @param bytes The array containing the bytes to append.
     * @param offset The initial offset of the array.
     * @param count The number of bytes to append.
     */
    void append(byte[] bytes, int offset, int count);

    /**
     * Appends the given bytes to this byte buffer.
     * @param buffer The buffer containing the bytes to append.
     */
    void append(ByteBuffer buffer);

    /**
     * Rolls back this byte buffer to the last backup.
     */
    void rollback();

    /**
     * Updates this byte buffer's backup to match the currently stored data.
     */
    void updateBackup();
}
