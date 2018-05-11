package atunstall.server.io.api.fs;

/**
 * Models a file system.
 * @param <T> The type used to index files in the file system.
 */
public interface FileSystem<T> {
    /**
     * Returns the file mapped to the given path.
     * @param path The path of the file to get.
     * @return The file associated with this file system and the given path.
     */
    File<? extends T> getFile(T path);
}
