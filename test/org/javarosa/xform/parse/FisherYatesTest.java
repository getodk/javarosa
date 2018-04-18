package org.javarosa.xform.parse;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FisherYatesTest {
    private static final List<Integer> INPUT = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    @Test
    public void it_produces_a_list_with_the_same_elements_in_different_order() {
        assertNotEquals(INPUT, FisherYates.shuffle(INPUT));
    }

    @Test
    public void shuffling_an_empty_list_has_no_effect() {
        assertTrue(FisherYates.shuffle(emptyList()).isEmpty());
    }

    @Test
    public void different_random_seeds_output_different_predictable_outputs() {
        List<Integer> output1 = FisherYates.shuffle(INPUT, 33);
        assertEquals(Arrays.asList(3, 5, 4, 8, 2, 0, 1, 6, 9, 7), output1);

        List<Integer> output2 = FisherYates.shuffle(INPUT, 42);
        assertEquals(Arrays.asList(0, 5, 9, 1, 8, 4, 6, 3, 7, 2), output2);
    }

    @Test
    public void same_random_seeds_output_same_predictable_outputs() {
        List<Integer> output1 = FisherYates.shuffle(INPUT, 42);
        assertEquals(Arrays.asList(0, 5, 9, 1, 8, 4, 6, 3, 7, 2), output1);

        List<Integer> output2 = FisherYates.shuffle(INPUT, 42);
        assertEquals(Arrays.asList(0, 5, 9, 1, 8, 4, 6, 3, 7, 2), output2);
    }
}