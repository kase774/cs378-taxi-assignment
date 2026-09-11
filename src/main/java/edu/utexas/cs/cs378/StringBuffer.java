package edu.utexas.cs.cs378;

import lombok.SneakyThrows;

import java.io.Writer;
import java.util.concurrent.locks.LockSupport;

public class StringBuffer {
    int bufferCount;
    // rotating buffer system
    String[][] buffers;
    int stringsPerBuffer;
    int allowReadFrom = 0;
    volatile int maxWriteToBuffer;
    boolean keepRunning = true;

    public StringBuffer(int bufferCount, int stringsPerBuffer) {
        this.bufferCount = bufferCount;
        this.stringsPerBuffer = stringsPerBuffer;
        this.buffers = new String[bufferCount][stringsPerBuffer];
        this.maxWriteToBuffer = bufferCount - 1;
    }

    @SneakyThrows
    public void writeIndex(int stringIndex, String computedResult) {
        int bufferIndex = stringIndex / stringsPerBuffer;
        while (maxWriteToBuffer < bufferIndex) {
            LockSupport.parkNanos(1);
        }
        int rotatedBufferIndex = bufferIndex % bufferCount;
        buffers[rotatedBufferIndex][stringIndex % stringsPerBuffer] = computedResult;
    }

    @SneakyThrows
    public void readTo(Writer writer) {
        int bufferIndex = allowReadFrom % bufferCount;
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
        allowReadFrom++;
        maxWriteToBuffer++;
    }

    public void onEnd() {
        keepRunning = false;
    }
}
