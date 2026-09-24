package edu.utexas.cs.cs378;

import lombok.SneakyThrows;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Stream;

// week 1
@SuppressWarnings("unused")

// same implementation as last week's string rotating buffer, just using byte[] now.
// week3 - optimized to use 1 byte[] instead (since all elements are same size)
// for each buffer
// also changed it so that there's no dedicated thread (since our machines now only
// have 2 threads total, so it makes more sense to have the other thread participate
// in the work from the stream). Therefore, networking work is distributed among all the
// threads
public class BinaryRotatingBufferStream {
    // rotating buffer system
    // we load 1 buffer at a time to the output stream and have
    // bufferCount - 1 full buffers to help ensure that we always have something to load
    private final byte[][] buffers;
    private final int elementsPerBuffer;
    private final int bufferCount;
    private final int sizePerElement;

    // number of elements written to each buffer (starting from writtenBufferIndex)
    private final AtomicIntegerArray bufferStatus;

    // the lowest buffer index such that not all of our data is written
    private final AtomicInteger finishedBufferIndex = new AtomicInteger(-1);
    // always smaller than finishedBufferIndex
    // the highest buffer index that we have written to
    private final AtomicInteger writtenBufferIndex = new AtomicInteger(-1);
    // lock to ensure that we only have 1 thread that's writing
    private final AtomicBoolean hasThreadWriting = new AtomicBoolean(false);
    private final OutputStream writeTo;

    public BinaryRotatingBufferStream(int bufferCount,
                                      int sizePerElement,
                                      int bufferSize,
                                      OutputStream writeTo) {
        this.bufferCount = bufferCount;
        this.sizePerElement = sizePerElement;
        this.elementsPerBuffer = bufferSize / sizePerElement;
        this.writeTo = writeTo;
        this.buffers = new byte[bufferCount][bufferSize];
        this.bufferStatus = new AtomicIntegerArray(bufferCount);
    }

    // if we are at -1, we can write up to 2 (assuming size 3)
    private int getMaximumIndexCanWriteTo() {
        return writtenBufferIndex.get() + bufferCount;
    }

    @SneakyThrows
    public void write(int index, byte[] computedResult) {
        if (computedResult.length != sizePerElement) {
            throw new IllegalArgumentException("Invalid byte array length");
        }
        int bufferIndex = index / elementsPerBuffer;
        int offsetCount = index % elementsPerBuffer;
        while (bufferIndex > getMaximumIndexCanWriteTo()) {
            attemptOutput();
            Thread.onSpinWait();
        }
        // becomes slot
        bufferIndex %= bufferCount;
        System.arraycopy(computedResult, 0, buffers[bufferIndex],
                offsetCount * sizePerElement, sizePerElement);
        if (bufferStatus.incrementAndGet(bufferIndex) == elementsPerBuffer) {
            attemptIncreaseFinishedIndex();
            attemptOutput();
        }
    }

    // scan future buffers and see if we can increase our finishedBufferIndex
    synchronized public void attemptIncreaseFinishedIndex() {
        for (int i = finishedBufferIndex.get() + 1; i <= getMaximumIndexCanWriteTo(); i++) {
            if (bufferStatus.get(i % bufferCount) == elementsPerBuffer) {
                finishedBufferIndex.incrementAndGet();
            } else break;
        }
    }

    @SneakyThrows
    public boolean attemptOutput() {
        if (!hasThreadWriting.compareAndSet(false, true)) return false;
        if (finishedBufferIndex.get() > writtenBufferIndex.get()) {
            int bufferIndex = (writtenBufferIndex.get() + 1) % buffers.length;
            int numElements = bufferStatus.get(bufferIndex);
            writeTo.write(buffers[bufferIndex], 0, numElements * sizePerElement);
            bufferStatus.set(bufferIndex, 0);
            writtenBufferIndex.getAndIncrement();
            hasThreadWriting.set(false);
            return numElements == elementsPerBuffer;
        }
        hasThreadWriting.set(false);
        return false;
    }

    @SneakyThrows
    public int takeIn(Stream<byte[]> stream) {
        AtomicInteger current = new AtomicInteger(0);
        stream.forEach(data -> write(current.getAndIncrement(), data));

        // mark the final buffer as finished even if it's not full
        if (bufferStatus.get((finishedBufferIndex.get() + 1) % bufferCount) != 0)
            finishedBufferIndex.incrementAndGet();

        //noinspection StatementWithEmptyBody
        while (attemptOutput());
        writeTo.flush();

        return current.get();
    }
}
