/*
 * Copyright 2019 Nafundi
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

package org.javarosa.core.model.instance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.Scenario.getRef;

import org.junit.Test;

public class TreeReferenceGenericizeTest {
    @Test
    public void genericize_sets_all_steps_with_unbound_multiplicity() {
        assertThat(getRef("/foo/bar").genericize(), is(getRef("/foo/bar")));

        TreeReference originalRef = getRef("/foo[3]/bar[4]");
        assertThat(originalRef.getMultiplicity(0), is(2));
        assertThat(originalRef.getMultiplicity(1), is(3));

        TreeReference genericizedRef = originalRef.genericize();
        for (int i = 0; i < originalRef.size(); i++)
            assertThat(genericizedRef.getMultiplicity(i), is(-1));
    }
}
