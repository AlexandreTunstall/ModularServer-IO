package atunstall.server.io.impl.util;

import atunstall.server.io.api.ParsableByteBuffer;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ArrayParsableByteBuffer implements ParsableByteBuffer {
    byte[] buffer;
    int index;
    int length;
    long consumed;
    private final Set<Partition> partitions;

    ArrayParsableByteBuffer(byte[] buffer) {
        this.buffer = buffer;
        partitions = new HashSet<>();
        index = 0;
        length = buffer.length;
    }

    @Override
    public long count() {
        return length;
    }

    @Override
    public byte get(long index) {
        int intIndex = (int) index;
        validateArgs(index, 1);
        return buffer[this.index + intIndex];
    }

    @Override
    public void get(long index, byte[] bytes, int offset, int count) {
        validateArgs(index, count);
        System.arraycopy(buffer, this.index + (int) index, bytes, offset, count);
    }

    @Override
    public void apply(long index, long count, Consumer<byte[]> consumer) {
        validateArgs(index, count);
        int intCount = (int) count;
        byte[] copy = new byte[intCount];
        System.arraycopy(buffer, this.index + (int) index, copy, 0, intCount);
        consumer.accept(copy);
    }

    @Override
    public long find(long index, byte[] sequence) {
        validateArgs(index, 0);
        int intIndex = (int) index;
        int seqIndex = 0;
        while (seqIndex < sequence.length && intIndex <= length - sequence.length + seqIndex) {
            if (buffer[this.index + intIndex++] == sequence[seqIndex]) {
                seqIndex++;
            } else {
                seqIndex = 0;
            }
        }
        if (seqIndex < sequence.length) {
            throw new IllegalStateException("sequence missing");
        }
        return intIndex - seqIndex;
    }

    @Override
    public boolean compare(long index, byte[] sequence) {
        validateArgs(index, sequence.length);
        int intIndex = (int) index;
        for (byte value : sequence) {
            if (buffer[this.index + intIndex++] != value) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void consume(long index, long count) {
        validateArgs(index, count);
        consumed += count;
        int intIndex = (int) index, intCount = (int) count;
        if (index == 0L) {
            this.index += intCount;
        } else if (intIndex + intCount / 2 < length / 2) {
            System.arraycopy(buffer, this.index, buffer, this.index += count, intIndex);
        } else if (index != length - count) {
            System.arraycopy(buffer, this.index + intIndex + intCount, buffer, this.index + intIndex, length - intIndex - intCount);
        }
        this.length -= intCount;
        partitions.forEach(p -> p.onConsume(intIndex, intCount));
    }

    @Override
    public void consumeAll() {
        consumed += length;
        partitions.forEach(p -> p.onConsume(0, length));
        length = 0;
    }

    @Override
    public long bytesConsumed() {
        return consumed;
    }

    @Override
    public ParsableByteBuffer partition(long index, long count) {
        if (index > Integer.MAX_VALUE || index + count > length) {
            throw new ArrayIndexOutOfBoundsException("index is too large");
        }
        Partition partition = new Partition((int) index, (int) count);
        partitions.add(partition);
        return partition;
    }

    @Override
    public void clear(long index, long count) {
        validateArgs(index, count);
        Arrays.fill(buffer, (int) index, (int) (index + count), (byte) 0);
    }

    @Override
    public String toString(long index, long count, Charset charset) {
        if (index > Integer.MAX_VALUE || count > Integer.MAX_VALUE || index + count > length) {
            throw new ArrayIndexOutOfBoundsException("index is too large");
        }
        return new String(buffer, this.index + (int) index, (int) count, charset);
    }

    void reset() {
        consumed = 0L;
        partitions.forEach(p -> p.consumed = 0L);
    }

    private void validateArgs(long index, long count) {
        if (index + count > length || index + count > Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("index is too large");
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("index is negative");
        }
    }

    private class Partition implements ParsableByteBuffer {
        private int partitionIndex;
        private int partitionLength;
        long consumed;

        private Partition(int partitionIndex, int partitionLength) {
            this.partitionIndex = partitionIndex;
            this.partitionLength = partitionLength;
        }

        @Override
        public long count() {
            return partitionLength;
        }

        @Override
        public byte get(long index) {
            validateArgs(index, 1L);
            return ArrayParsableByteBuffer.this.get(partitionIndex + index);
        }

        @Override
        public void get(long index, byte[] bytes, int offset, int count) {
            validateArgs(index, count);
            ArrayParsableByteBuffer.this.get(partitionIndex + index, bytes, offset, count);
        }

        @Override
        public void apply(long index, long count, Consumer<byte[]> consumer) {
            validateArgs(index, count);
            ArrayParsableByteBuffer.this.apply(partitionIndex + index, count, consumer);
        }

        @Override
        public long find(long index, byte[] sequence) {
            validateArgs(index, 1L);
            long result = ArrayParsableByteBuffer.this.find(partitionIndex + index, sequence) - partitionIndex;
            if (result >= partitionLength) {
                throw new IllegalStateException("sequence missing");
            }
            return result;
        }

        @Override
        public boolean compare(long index, byte[] sequence) {
            validateArgs(index, 1L);
            return ArrayParsableByteBuffer.this.compare(partitionIndex + index, sequence);
        }

        @Override
        public void consume(long index, long count) {
            validateArgs(index, count);
            ArrayParsableByteBuffer.this.consume(partitionIndex + index, count);
        }

        @Override
        public void consumeAll() {
            ArrayParsableByteBuffer.this.consume(partitionIndex, partitionLength);
        }

        @Override
        public long bytesConsumed() {
            return 0;
        }

        @Override
        public ParsableByteBuffer partition(long index, long count) {
            validateArgs(index, count);
            return ArrayParsableByteBuffer.this.partition(partitionIndex + index, count);
        }

        @Override
        public void clear(long index, long count) {
            validateArgs(index, count);
            ArrayParsableByteBuffer.this.clear(partitionIndex + index, count);
        }

        @Override
        public String toString(long index, long count, Charset charset) {
            validateArgs(index, count);
            return ArrayParsableByteBuffer.this.toString(partitionIndex + index, count, charset);
        }

        private void validateArgs(long index, long count) {
            if (index + count > partitionLength) {
                throw new ArrayIndexOutOfBoundsException("index is too large");
            } else if (index < 0) {
                throw new ArrayIndexOutOfBoundsException("index is negative");
            }
        }

        private void onConsume(int index, int count) {
            partitionIndex = partitionIndex - (Math.min(count + index, partitionIndex) - Math.min(index, partitionIndex));
            if (index + count > partitionIndex) {
                int consumed = Math.max((Math.min(count + index, partitionLength + partitionIndex) - Math.max(index, partitionIndex)), 0);
                partitionLength -= consumed;
                this.consumed += consumed;
            }
        }
    }
}
