package org.javarosa.xform.parse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParkMillerTest {
    @Test
    public void same_seeds_produce_same_predictable_numbers() {
        ParkMiller random1 = new ParkMiller(42);
        ParkMiller random2 = new ParkMiller(42);

        // We test the sequence of the first million integers
        for (int i = 0; i < 1_000_000; i++) {
            assertEquals(random1.nextInt(), random2.nextInt());
            assertEquals(random1.nextDouble(), random2.nextDouble(), 0.0);
        }
    }

    @Test
    public void it_admits_negative_seeds() {
        ParkMiller random1 = new ParkMiller(-42);
        ParkMiller random2 = new ParkMiller(-42);

        // We test the sequence of the first million integers
        for (int i = 0; i < 1_000_000; i++) {
            assertEquals(random1.nextInt(), random2.nextInt());
            assertEquals(random1.nextDouble(), random2.nextDouble(), 0.0);
        }
    }
}