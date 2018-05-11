package atunstall.server.io.impl.fs;

import atunstall.server.core.api.Module;
import atunstall.server.core.api.Version;
import atunstall.server.core.api.logging.Level;
import atunstall.server.core.api.logging.Logger;
import atunstall.server.io.api.util.ArrayStreams;
import atunstall.server.io.api.fs.File;
import atunstall.server.io.api.fs.JavaFileSystem;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Module
public class JavaFileSystemImpl implements JavaFileSystem {
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("IO-JavaFS");
    private final Logger logger;
    private final Executor executor;
    private final ArrayStreams arrayStreams;
    private final Map<Path, WeakReference<File<Path>>> cache;
    private final AtomicLong threadCount;

    public JavaFileSystemImpl(@Version(major = 1, minor = 0) Logger logger, @Version(major = 1, minor = 0) ArrayStreams arrayStreams) {
        this.logger = logger.getChild("Java FS");
        this.executor = Executors.newCachedThreadPool(this::newThread);
        this.arrayStreams = arrayStreams;
        cache = new HashMap<>();
        threadCount = new AtomicLong(0L);
    }

    @Override
    public File<? extends Path> getFile(Path path) {
        File<Path> file = cache.containsKey(path) ? cache.get(path).get() : null;
        if (file == null) {
            file = new JavaFile(this, path);
            cache.put(path, new WeakReference<>(file));
        }
        return file;
    }


    Logger getLogger() {
        return logger;
    }

    Executor getExecutor() {
        return executor;
    }

    ArrayStreams getArrayStreams() {
        return arrayStreams;
    }

    private Thread newThread(Runnable r) {
        Thread thread = new Thread(THREAD_GROUP, r, THREAD_GROUP.getName() + "-" + threadCount.getAndIncrement());
        thread.setDaemon(true);
        logger.log(Level.DEBUG, "Creating thread " + thread.getName());
        return thread;
    }
}
