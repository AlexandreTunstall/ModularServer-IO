package atunstall.server.io.api.fs;

import atunstall.server.io.api.InputStream;
import atunstall.server.io.api.OutputStream;

import java.util.Optional;

/**
 * Models a file on a file system.
 */
public interface File<T> {
    /**
     * Returns this file's file system.
     * @return The file system associated with this file.
     */
    FileSystem<T> getFileSystem();

    /**
     * Creates a new input stream bound to this file.
     * @return The created input stream.
     */
    InputStream newInputStream();

    /**
     * Creates a new output stream to write to this file.
     * The returned stream is thread-safe.
     * If this file already exists, it will be overwritten when it is written to.
     * @return The output stream that should be used to write to this file.
     */
    OutputStream newOutputStream();

    /**
     * Checks whether this file exists in the file system.
     * @return True if it exists, false otherwise.
     */
    boolean exists();

    /**
     * Checks whether this file does not exist in the file system.
     * This is necessary as {@link #exists()} will return false if the existence of the file cannot be determined.
     * @return True if it does not exist, false otherwise.
     */
    boolean notExists();

    /**
     * Checks whether this file can be read.
     * This is preferred over an existence check as the file may exist but not be readable.
     * The result of this method is immediately outdated.
     * @return True if this file can be read, false otherwise.
     */
    boolean canRead();

    /**
     * Checks whether this file can be written to.
     * @return True if this file can be written to, false otherwise.
     */
    boolean canWrite();

    /**
     * Attempts to get the MIME type of this file.
     * @return The MIME type of the file if known, {@link Optional#empty()} otherwise.
     */
    Optional<String> getMimeType();

    /**
     * Returns the size of this file.
     * @return The size of the file in bytes.
     */
    long getSize();
}
