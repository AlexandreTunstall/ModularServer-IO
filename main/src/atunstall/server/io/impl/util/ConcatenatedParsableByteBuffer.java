package atunstall.server.io.impl.util;

import atunstall.server.io.api.ParsableByteBuffer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConcatenatedParsableByteBuffer implements ParsableByteBuffer {
    private final ArrayStreamsImpl streams;
    private final List<ParsableByteBuffer> buffers;

    ConcatenatedParsableByteBuffer(ArrayStreamsImpl streams, List<ParsableByteBuffer> buffers) {
        this.streams = streams;
        this.buffers = buffers;
    }

    @Override
    public long find(long index, byte[] sequence) {
        ParsableByteBuffer previous = null;
        long offset = 0L;
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                previous = current;
                offset += current.count();
                index -= current.count();
                continue;
            }
            if (previous != null) {
                int underflow = (int) Math.min(sequence.length - 1, previous.count() - index);
                int overflow = (int) Math.min(sequence.length - 1, current.count());
                if (underflow + overflow >= sequence.length) {
                    int seqIndex = 0;
                    byte[] buffer = new byte[underflow + overflow];
                    previous.get(previous.count() - underflow, buffer, 0, underflow);
                    current.get(0L, buffer, underflow, overflow);
                    int findIndex = 0;
                    while (seqIndex < sequence.length && findIndex <= overflow + underflow - sequence.length + seqIndex) {
                        if (buffer[findIndex++] == sequence[seqIndex]) {
                            seqIndex++;
                        } else {
                            seqIndex = 0;
                        }
                    }
                    if (seqIndex == sequence.length) {
                        return offset + findIndex - sequence.length;
                    }
                }
            }
            try {
                return offset + current.find(index, sequence);
            } catch (RuntimeException ignored) {}
            previous = current;
            offset += current.count();
            index = 0L;
        }
        throw new IllegalArgumentException("byte sequence not found");
    }

    @Override
    public boolean compare(long index, byte[] sequence) {
        if (sequence.length == 0) return true;
        ParsableByteBuffer previous = null;
        long previousIndex = -1L;
        byte[] buffer = new byte[sequence.length * 2 - 1];
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                previous = current;
                index -= current.count();
                continue;
            }
            if (previous != null && previousIndex >= 0) {
                int underflow = (int) (previous.count() - previousIndex);
                previous.get(previousIndex, buffer, 0, underflow);
                current.get(0L, buffer, underflow, sequence.length - underflow);
                int findIndex = 0;
                for (byte value : sequence) {
                    if (buffer[findIndex++] != value) {
                        return false;
                    }
                }
                return true;
            }
            try {
                return current.compare(index, sequence);
            } catch (RuntimeException ignored) {
                previousIndex = index;
            }
            previous = current;
            index = 0L;
        }
        return false;
    }

    @Override
    public void consume(long index, long count) {
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                index -= current.count();
                continue;
            }
            long currentCount = Math.min(count, current.count() - index);
            current.consume(index, currentCount);
            if ((count -= currentCount) == 0) {
                return;
            }
            index = 0L;
        }
        throw new IllegalArgumentException("index out of range");
    }

    @Override
    public void consumeAll() {
        buffers.forEach(ParsableByteBuffer::consumeAll);
    }

    @Override
    public long bytesConsumed() {
        return 0;
    }

    @Override
    public ParsableByteBuffer partition(long index, long count) {
        List<ParsableByteBuffer> partitionBuffers = new ArrayList<>();
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                index -= current.count();
                continue;
            }
            long currentCount = Math.min(count, current.count() - index);
            partitionBuffers.add(current.partition(index, currentCount));
            if ((count -= currentCount) == 0) {
                return streams.concatenate(partitionBuffers);
            }
            index = 0L;
        }
        throw new IllegalArgumentException("index out of range");
    }

    @Override
    public void clear(long index, long count) {
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                index -= current.count();
                continue;
            }
            long currentCount = Math.min(count, current.count() - index);
            current.clear(index, currentCount);
            if ((count -= currentCount) == 0L) {
                return;
            }
            index = 0L;
        }
        if (count > 0) {
            throw new IllegalArgumentException("index out of range");
        }
    }

    @Override
    public String toString(long index, long count, Charset charset) {
        if (count == 0L) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                index -= current.count();
                continue;
            }
            long currentCount = Math.min(count, current.count() - index);
            result.append(current.toString(index, currentCount, charset));
            if ((count -= currentCount) == 0) {
                return result.toString();
            }
            index = 0L;
        }
        throw new IllegalArgumentException("index out of range");
    }

    @Override
    public long count() {
        return buffers.stream().mapToLong(ParsableByteBuffer::count).sum();
    }

    @Override
    public byte get(long index) {
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                index -= current.count();
                continue;
            }
            return current.get(index);
        }
        throw new IllegalArgumentException("index out of range");
    }

    @Override
    public void get(long index, byte[] bytes, int offset, int count) {
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                index -= current.count();
                continue;
            }
            int currentCount = (int) Math.min(count, current.count() - index);
            current.get(index, bytes, offset, currentCount);
            if ((count -= currentCount) == 0) {
                return;
            }
            index = 0L;
        }
        if (count > 0) {
            throw new IllegalArgumentException("index out of range");
        }
    }

    @Override
    public void apply(long index, long count, Consumer<byte[]> consumer) {
        for (ParsableByteBuffer current : buffers) {
            if (index >= current.count()) {
                index -= current.count();
                continue;
            }
            long currentCount = Math.min(count, current.count() - index);
            current.apply(index, currentCount, consumer);
            if ((count -= currentCount) == 0L) {
                return;
            }
            index = 0L;
        }
        if (count > 0) {
            throw new IllegalArgumentException("index out of range");
        }
    }
}
