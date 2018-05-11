package atunstall.server.io.api.fs;

import atunstall.server.core.api.Unique;
import atunstall.server.core.api.Version;

import java.nio.file.Path;

/**
 * Provides a bridge between the Java FS API and the server FS API.
 */
@Version(major = 1, minor = 0)
@Unique
public interface JavaFileSystem extends FileSystem<Path> {
    // Empty
}
