package edu.utexas.cs.cs378.week1specific;

import lombok.SneakyThrows;

import java.io.Writer;
import java.util.concurrent.locks.LockSupport;

// week 1
@SuppressWarnings("unused")
public class StringRotatingBuffer {
    private final int bufferCount;
    // rotating buffer system
    // we load 1 buffer at a time to the writer and have
    // bufferCount - 1 full buffers to help ensure that we always have to something to load
    private final String[][] buffers;
    private final int stringsPerBuffer;

    // the nth buffer we are reading from; the index is readingFrom % bufferCount
    private int readingFrom = 0;
    // the maximum index that we can write to
    private volatile int writingTo;
    private boolean keepRunning = true;

    public StringRotatingBuffer(int bufferCount, int stringsPerBuffer) {
        this.bufferCount = bufferCount;
        this.stringsPerBuffer = stringsPerBuffer;
        this.buffers = new String[bufferCount][stringsPerBuffer];
        this.writingTo = bufferCount - 1; // initialize so that we can write to all of them
    }

    @SneakyThrows
    public void writeTo(int stringIndex, String computedResult) {
        int bufferIndex = stringIndex / stringsPerBuffer;
        while (writingTo < bufferIndex) {
            // stall
            LockSupport.parkNanos(1);
        }
        int rotatedBufferIndex = bufferIndex % bufferCount;
        buffers[rotatedBufferIndex][stringIndex % stringsPerBuffer] = computedResult;
    }

    @SneakyThrows
    public boolean moveDataTo(Writer writer) {
        int bufferIndex = readingFrom % bufferCount;
        for (int index = 0; index < stringsPerBuffer; index++) {
            String str;
            while ((str = buffers[bufferIndex][index]) == null && keepRunning) {
                Thread.onSpinWait();
            }
            if (!keepRunning) break;
            writer.write(str);
            buffers[bufferIndex][index] = null;
            writer.write('\n');
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
}
