package org.javarosa.xform.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class FisherYatesTest {
    private static final List<Integer> INPUT = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    @Test
    public void it_produces_a_list_with_the_same_elements_in_different_order() {
        List<Integer> output = new ArrayList<>(INPUT);
        FisherYates.shuffle(output, new Random());
        assertNotEquals(INPUT, output);
    }

    @Test
    public void shuffling_an_empty_list_has_no_effect() {
        List<Object> emptyList = Collections.emptyList();
        FisherYates.shuffle(emptyList, new Random());
        assertTrue(emptyList.isEmpty());
    }

    @Test
    public void different_random_seeds_output_different_predictable_outputs() {
        List<Integer> output1 = new ArrayList<>(INPUT);
        FisherYates.shuffle(output1, new Random(42));
        assertEquals(Arrays.asList(2, 3, 7, 4, 8, 5, 0, 9, 1, 6), output1);

        List<Integer> output2 = new ArrayList<>(INPUT);
        FisherYates.shuffle(output2, new Random(33));
        assertEquals(Arrays.asList(0, 8, 1, 6, 7, 2, 5, 4, 9, 3), output2);
    }

    @Test
    public void same_random_seeds_output_same_predictable_outputs() {
        List<Integer> output1 = new ArrayList<>(INPUT);
        FisherYates.shuffle(output1, new Random(42));
        assertEquals(Arrays.asList(2, 3, 7, 4, 8, 5, 0, 9, 1, 6), output1);

        List<Integer> output2 = new ArrayList<>(INPUT);
        FisherYates.shuffle(output2, new Random(42));
        assertEquals(Arrays.asList(2, 3, 7, 4, 8, 5, 0, 9, 1, 6), output2);
    }
}