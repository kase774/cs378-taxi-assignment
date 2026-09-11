package edu.utexas.cs.cs378;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class RotatingStringQueueTest {

    @Test
    void readsPartialFinalBufferThenStops() {
        int total = 1_003;
        int bufferCount = 4;
        int stringsPerBuffer = 16;
        StringBuffer queue = new StringBuffer(bufferCount, stringsPerBuffer);
        queue.finish(total);

        StringWriter writer = new StringWriter();
        Thread producer = new Thread(() -> {
            for (int i = 0; i < total; i++) {
                queue.writeIndex(i, "L" + i);
            }
        });

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            producer.start();
            while (queue.readTo(writer)) {
                // keep draining
            }
            producer.join();
        });

        Set<String> lines = new HashSet<>();
        for (String line : writer.toString().split("\n", -1)) {
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
        assertEquals(total, lines.size());
        assertFalse(queue.readTo(new StringWriter()));
    }
}
