package atunstall.server.io.impl.fs;

import atunstall.server.core.api.logging.Level;
import atunstall.server.io.api.InputStream;
import atunstall.server.io.api.OutputStream;
import atunstall.server.io.api.ParsableByteBuffer;
import atunstall.server.io.api.fs.File;
import atunstall.server.io.api.fs.FileSystem;
import atunstall.server.io.api.util.AppendableParsableByteBuffer;
import atunstall.server.io.impl.util.HandledInputStreamImpl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.function.Consumer;

public class JavaFile implements File<Path> {
    private final JavaFileSystemImpl fs;
    private final Path path;

    JavaFile(JavaFileSystemImpl fs, Path path) {
        this.fs = fs;
        this.path = path;
    }

    @Override
    public FileSystem<Path> getFileSystem() {
        return fs;
    }

    @Override
    public InputStream newInputStream() {
        return new InputStreamImpl();
    }

    @Override
    public OutputStream newOutputStream() {
        return new OutputStreamImpl();
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public boolean notExists() {
        return Files.notExists(path);
    }

    @Override
    public boolean canRead() {
        return Files.isReadable(path);
    }

    @Override
    public boolean canWrite() {
        return Files.isWritable(path);
    }

    @Override
    public Optional<String> getMimeType() {
        try {
            return Optional.ofNullable(Files.probeContentType(path));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to probe content type", e);
        }
    }

    @Override
    public long getSize() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to get file size", e);
        }
    }

    private class OutputStreamImpl implements OutputStream {
        private BufferedOutputStream stream;
        private byte[] buffer;

        private OutputStreamImpl() {
            try {
                stream = new BufferedOutputStream(Files.newOutputStream(path));
            } catch (IOException e) {
                throw new RuntimeException("exception opening the stream", e);
            }
            buffer = new byte[4096];
        }

        @Override
        public void close() throws Exception {
            stream.close();
        }

        @Override
        public void accept(atunstall.server.io.api.ByteBuffer byteBuffer) {
            long index = 0;
            long count = byteBuffer.count();
            try {
                while (index < count) {
                    int read = (int) Math.min(buffer.length, count - index);
                    byteBuffer.get(index, buffer, 0, read);
                    stream.write(buffer, 0, read);
                    index += read;
                }
                stream.flush();
            } catch (IOException e) {
                throw new RuntimeException("failed to write to the stream", e);
            }
        }
    }

    private class InputStreamImpl extends HandledInputStreamImpl implements InputStream {
        private AsynchronousFileChannel input;
        private AppendableParsableByteBuffer buffer;
        private byte[] readBuffer;
        private ByteBuffer readBufferWrapper;
        private long position;

        private InputStreamImpl() {
            super(null);
            try {
                input = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            } catch (IOException e) {
                throw new UncheckedIOException("failed to open file channel", e);
            }
            buffer = fs.getArrayStreams().createByteBuffer(4096);
            readBufferWrapper = ByteBuffer.wrap(readBuffer = new byte[4096]);
            position = 0L;
            fs.getExecutor().execute(this::readFile);
        }

        @Override
        public void close() throws Exception {
            input.close();
        }

        @Override
        public boolean isClosed() {
            return !input.isOpen();
        }

        @Override
        public void queueConsumer(Consumer<? super ParsableByteBuffer> consumer) {
            super.queueConsumer(consumer);
            if (isClosed() && buffer.count() > 0L) {
                fs.getExecutor().execute(this::consumeUntilEmpty);
            }
        }

        private void readFile() {
            input.read(readBufferWrapper, position, null, new CompletionHandlerImpl());
        }

        private void safeClose() {
            try {
                close();
            } catch (Exception e) {
                fs.getLogger().log(Level.ERROR, "Failed to close channel", e);
            }
        }

        private void consumeUntilEmpty() {
            consumeSafe(buffer);
            if (buffer.count() > 0L && consumerCount() > 0) {
                fs.getExecutor().execute(this::consumeUntilEmpty);
            }
        }

        private class CompletionHandlerImpl implements CompletionHandler<Integer, Void> {
            @Override
            public void completed(Integer result, Void attachment) {
                if (result == -1) {
                    safeClose();
                    consumeUntilEmpty();
                    return;
                }
                buffer.append(readBuffer, 0, result);
                position += result;
                if (isClosed()) {
                    consumeUntilEmpty();
                    return;
                }
                consumeSafe(buffer);
                input.read(readBufferWrapper, position, null, new CompletionHandlerImpl());
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                fs.getLogger().log(Level.ERROR, "Failed to read file", exc);
                safeClose();
                consumeUntilEmpty();
            }
        }
    }
}
