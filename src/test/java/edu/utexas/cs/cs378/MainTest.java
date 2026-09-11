package edu.utexas.cs.cs378;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void maxDuration() throws Exception {
        assertEquals(10800, Main.maxDuration("taxi-data-sorted-small.csv"));
    }

    @Test
    void maxDistance() throws Exception {
        assertEquals(9585, Main.maxDistance("taxi-data-sorted-small.csv"));
    }

    @Test
    void maxCosts() throws Exception {
        assertArrayEquals(new long[]{46500, 1250, 50, 17700, 2000, 46500}, Main.maxCosts());
    }
}
