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

package org.javarosa.xpath;

import static junit.framework.TestCase.assertFalse;
import static org.javarosa.xpath.XPathNodeset.shuffle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Test;import org.junit.Before;

public class XPathNodesetShuffleTest {
    @Test
    public void given_an_empty_nodeset_randomize_outputs_a_new_empty_nodeset() {
        XPathNodeset input = new XPathNodeset(new ArrayList<>(), null, null);
        XPathNodeset output = shuffle(input);

        assertNotSame(input, output);
        assertEquals(0, output.size());
    }

    @Test
    public void given_a_non_empty_nodeset_randomize_outputs_a_nodeset_with_same_elements_shuffled() {
        XPathNodeset input = getInputNodeset();
        XPathNodeset output = shuffle(input);

        assertTrue(nodesEqualInAnyOrder(input, output));
        assertFalse(nodesEqualInOrder(input, output));
        // Although, in fact, this could happen, because it's random.
    }

    @Test
    public void given_the_same_seed_randomize_should_output_nodesets_with_the_same_elements_in_the_same_order() {
        XPathNodeset input = getInputNodeset();
        XPathNodeset output1 = shuffle(input, 42L);
        XPathNodeset output2 = shuffle(input, 42L);

        assertTrue(nodesEqualInOrder(output1, output2));
    }

    private static boolean nodesEqualInOrder(XPathNodeset left, XPathNodeset right) {
        if (left.size() != right.size())
            return false;

        for (int i = 0; i < left.size(); i++)
            if (left.getRefAt(i) != right.getRefAt(i))
                return false;

        return true;
    }

    private static boolean nodesEqualInAnyOrder(XPathNodeset left, XPathNodeset right) {
        if (left.size() != right.size())
            return false;

        Set<TreeReference> leftRefs = new HashSet<>();
        Set<TreeReference> rightRefs = new HashSet<>();
        for (int i = 0; i < left.size(); i++) {
            leftRefs.add(left.getRefAt(i));
            rightRefs.add(right.getRefAt(i));
        }

        return leftRefs.containsAll(rightRefs);
    }

    private static XPathNodeset getInputNodeset() {
        List<TreeReference> refs = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            refs.add(new TreeReference());
        return new XPathNodeset(refs, null, null);
    }
}
