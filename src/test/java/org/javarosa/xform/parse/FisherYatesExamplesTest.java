package org.javarosa.xform.parse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.javarosa.xform.parse.FisherYatesTest.list;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FisherYatesExamplesTest {
    @Parameterized.Parameter(value = 0)
    public String testName;

    @Parameterized.Parameter(value = 1)
    public List<?> input;

    @Parameterized.Parameter(value = 2)
    public Long seed;

    @Parameterized.Parameter(value = 3)
    public List<?> expectedOutput;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"Numbers [0-9], seed 33", list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 33L, list(3, 5, 4, 8, 2, 0, 1, 6, 9, 7)},
            {"Numbers [0-9], seed 42", list(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 42L, list(0, 5, 9, 1, 8, 4, 6, 3, 7, 2)},
            {"Letters [A-F], seed 42", list("ABCDEF"), 42L, list("AFCBDE")},
            {"Letters [A-F], seed -42", list("ABCDEF"), -42L, list("EDAFBC")},
            {"Letters [A-F], seed 1", list("ABCDEF"), 1L, list("BFEACD")},
            {"Letters [A-F], seed 11111111", list("ABCDEF"), 11111111L, list("ACDBFE")},
        });
    }

    @Test
    public void seeded_shuffle_produces_predictable_outputs() {
        assertEquals(expectedOutput, FisherYates.shuffle(input, seed));
    }
}