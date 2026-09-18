// modified in week 2
package edu.utexas.cs.cs378;

import lombok.SneakyThrows;

import java.io.OutputStream;
import java.util.concurrent.locks.LockSupport;

// week 1
@SuppressWarnings("unused")

// same implementation as last week's string rotating buffer, just using byte[] now.
public class BinaryRotatingBuffer {
    private final int bufferCount;
    // rotating buffer system
    // we load 1 buffer at a time to the output stream and have
    // bufferCount - 1 full buffers to help ensure that we always have something to load
    private final byte[][][] buffers;
    private final int byteArraysPerBuffer;

    // the nth buffer we are reading from; the index is readingFrom % bufferCount
    private int readingFrom = 0;
    // the maximum index that we can write to
    private volatile int writingTo;
    private boolean keepRunning = true;

    public BinaryRotatingBuffer(int bufferCount, int byteArraysPerBuffer) {
        this.bufferCount = bufferCount;
        this.byteArraysPerBuffer = byteArraysPerBuffer;
        this.buffers = new byte[bufferCount][byteArraysPerBuffer][];
        this.writingTo = bufferCount - 1; // initialize so that we can write to all of them
    }

    @SneakyThrows
    public void writeTo(int byteArrayIndex, byte[] computedResult) {
        int bufferIndex = byteArrayIndex / byteArraysPerBuffer;
        while (writingTo < bufferIndex) {
            // stall
            LockSupport.parkNanos(1);
        }
        int rotatedBufferIndex = bufferIndex % bufferCount;
        buffers[rotatedBufferIndex][byteArrayIndex % byteArraysPerBuffer] = computedResult;
    }

    @SneakyThrows
    public boolean moveDataTo(OutputStream outputStream) {
        int bufferIndex = readingFrom % bufferCount;
        for (int index = 0; index < byteArraysPerBuffer; index++) {
            byte[] bytes;
            while ((bytes = buffers[bufferIndex][index]) == null && keepRunning) {
                Thread.onSpinWait();
            }
            if (!keepRunning) break;
            outputStream.write(bytes);
            buffers[bufferIndex][index] = null;
        }
        readingFrom++;
        // it's fine because only 1 thread calls this
        //noinspection NonAtomicOperationOnVolatileField
        writingTo++;
        return keepRunning;
    }

    public void writingFinished() {
        keepRunning = false;
    }

    public boolean isRunning() {
        return keepRunning;
    }
}
