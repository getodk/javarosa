package org.javarosa.xform.parse;

import static java.util.Collections.emptyList;
import static org.javarosa.xform.parse.FisherYates.shuffle;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FisherYatesTest {
    private static final List<Integer> INPUT = list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    @Test
    public void it_produces_a_list_with_the_same_elements_in_different_order() {
        assertNotEquals(INPUT, shuffle(INPUT));
    }

    @Test
    public void shuffling_an_empty_list_has_no_effect() {
        assertTrue(shuffle(emptyList()).isEmpty());
    }

    @Test
    public void different_random_seeds_output_different_outputs() {
        assertNotEquals(shuffle(INPUT, 33), shuffle(INPUT, 42));
    }

    @SafeVarargs
    static <T> List<T> list(T... values) {
        return Arrays.asList(values);
    }

    static List<String> list(String values) {
        return Arrays.asList(values.split(""));
    }
}