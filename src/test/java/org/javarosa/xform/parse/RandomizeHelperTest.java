/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javarosa.xform.parse;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotSame;
import static org.javarosa.xform.parse.RandomizeHelper.cleanNodesetDefinition;
import static org.javarosa.xform.parse.RandomizeHelper.shuffle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomizeHelperTest {
    @Test
    public void cleans_the_nodeset_definition() {
        // We will try different combinations of whitespace and seed presence around the path
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize(/some/path)"));
        assertEquals("/some/path",                              cleanNodesetDefinition(" randomize(/some/path)"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize(/some/path) "));
        assertEquals("/some/path",                              cleanNodesetDefinition(" randomize(/some/path) "));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize( /some/path )"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize(/some/path,33)"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize(/some/path, 33)"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize( /some/path , 33)"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize(/some/path, /some/other/path)"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize(/some/path , /some/other/path)"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize( /some/path, /some/other/path)"));
        assertEquals("/some/path",                              cleanNodesetDefinition("randomize( /some/path , /some/other/path)"));
        assertEquals("/some/path[someFilter]",                  cleanNodesetDefinition("randomize(/some/path[someFilter])"));
        assertEquals("/some/path[someFilter]",                  cleanNodesetDefinition("randomize(/some/path[someFilter], 33)"));
        assertEquals("/some/path[someFilter(with, commas)]",    cleanNodesetDefinition("randomize(/some/path[someFilter(with, commas)])"));
        assertEquals("/some/path[someFilter(with, commas)]",    cleanNodesetDefinition("randomize(/some/path[someFilter(with, commas)], 33)"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_when_cleaning_a_nodeset_that_does_not_use_randomize_variant_1() {
        cleanNodesetDefinition("this doesn't start with randomize( )");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_when_cleaning_a_nodeset_that_does_not_use_randomize_variant_2() {
        cleanNodesetDefinition("this doesn't end with ) *some filler here*");
    }

    @Test
    public void given_an_empty_list_shuffle_outputs_a_new_empty_list() {
        List<?> input = Collections.emptyList();
        List<?> output = shuffle(input);

        assertNotSame(input, output);
        assertEquals(0, output.size());
    }

    @Test
    public void given_a_non_empty_nodeset_randomize_outputs_a_nodeset_with_same_elements_shuffled() {
        List<Wrap<Integer>> input = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(Wrap::wrap).collect(toList());
        List<Wrap<Integer>> output = shuffle(input);

        assertEquals(input.size(), output.size());
        assertTrue(output.containsAll(input));
        assertTrue(nodesEqualInAnyOrder(input, output));
        assertFalse(nodesEqualInOrder(input, output));
        // Although, in fact, this could happen, because it's random.
    }

    @Test
    public void given_the_same_seed_randomize_should_output_nodesets_with_the_same_elements_in_the_same_order() {
        List<Wrap<Integer>> input = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(Wrap::wrap).collect(toList());
        List<Wrap<Integer>> output1 = shuffle(input, 42L);
        List<Wrap<Integer>> output2 = shuffle(input, 42L);

        assertNotSame(output1, output2);
        assertTrue(nodesEqualInOrder(output1, output2));
    }

    // We will use this class in the test just to force equality
    // rules to work by reference, not by value
    private static class Wrap<T> {
        private final T t;

        Wrap(T t) {
            this.t = t;
        }

        static <U> Wrap<U> wrap(U u) {
            return new Wrap<>(u);
        }
    }

    private static boolean nodesEqualInOrder(List<?> left, List<?> right) {
        if (left.size() != right.size())
            return false;

        for (int i = 0; i < left.size(); i++)
            if (!left.get(i).equals(right.get(i)))
                return false;

        return true;
    }

    private static boolean nodesEqualInAnyOrder(List<?> left, List<?> right) {
        if (left.size() != right.size())
            return false;

        return left.containsAll(right);
    }
}