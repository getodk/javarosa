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

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.instance.TestHelpers.buildRef;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TreeReferenceIsAncestorOfTest {
    private static final boolean IRRELEVANT = true;

    @Parameterized.Parameter(value = 0)
    public String testCase;

    @Parameterized.Parameter(value = 1)
    public String a;

    @Parameterized.Parameter(value = 2)
    public String b;

    @Parameterized.Parameter(value = 3)
    public boolean properParent;

    @Parameterized.Parameter(value = 4)
    public boolean expectedResult;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // Self references
            {"/foo is not parent of /foo if we exclude self references", "/foo", "/foo", true, false},
            {"/foo/bar is not parent of /foo/bar if we exclude self references", "/foo/bar", "/foo/bar", true, false},
            {"/foo is parent of /foo if we don't exclude self references", "/foo", "/foo", false, true},
            {"/foo/bar is  parent of /foo/bar if we don't exclude self references", "/foo/bar", "/foo/bar", false, true},

            // Simple scenarios
            {"/foo/bar is not parent of /foo", "/foo/bar", "/foo", IRRELEVANT, false},
            {"/foo is not parent of /bar", "/foo", "/bar", IRRELEVANT, false},
            {"foo is not parent of bar", "foo", "bar", IRRELEVANT, false},
            {"/foo is not parent of /bar/foo", "/foo", "/bar/foo", IRRELEVANT, false},
            {"/foo is parent of /foo/bar", "/foo", "/foo/bar", IRRELEVANT, true},
            {"/foo/bar is parent of /foo/bar/baz", "/foo/bar", "/foo/bar/baz", IRRELEVANT, true},

            // Ancestry (more than parent) scenarios
            {"/foo is parent of /foo/bar/baz", "/foo", "/foo/bar/baz", IRRELEVANT, true},

            // Scenarios with a mix of relative and absolute paths
            {"foo is not parent of /foo/bar", "foo", "/foo/bar", IRRELEVANT, false},
            {"/foo is not parent of foo/bar", "/foo", "foo/bar", IRRELEVANT, false},

            // We will consider undefined (no multiplicity predicate), [-1], and [0] multiplicities due to their special meaning and [2]
            // as an example of a specific non-special multiplicity
            // We will also consider first-level and following-level multiplicities, because the first level behaves differently

            // Multiplicity scenario: undefined ancestor is parent of any other multiplicity, regardless of level
            {"/foo is parent of /foo/bar", "/foo", "/foo/bar", IRRELEVANT, true},
            {"/foo is parent of /foo[-1]/bar", "/foo", "/foo[-1]/bar", IRRELEVANT, true},
            {"/foo is parent of /foo[0]/bar", "/foo", "/foo[0]/bar", IRRELEVANT, true},
            {"/foo is parent of /foo[2]/bar", "/foo", "/foo[2]/bar", IRRELEVANT, true},
            {"/foo/bar is parent of /foo/bar/baz", "/foo/bar", "/foo/bar/baz", IRRELEVANT, true},
            {"/foo/bar is parent of /foo/bar[-1]/baz", "/foo/bar", "/foo/bar[-1]/baz", IRRELEVANT, true},
            {"/foo/bar is parent of /foo/bar[0]/baz", "/foo/bar", "/foo/bar[0]/baz", IRRELEVANT, true},
            {"/foo/bar is parent of /foo/bar[2]/baz", "/foo/bar", "/foo/bar[2]/baz", IRRELEVANT, true},

            // Multiplicity scenario: [-1] ancestor is parent of any other multiplity, regardless of level
            {"/foo[-1] is parent of /foo/bar", "/foo[-1]", "/foo/bar", IRRELEVANT, true},
            {"/foo[-1] is parent of /foo[-1]/bar", "/foo[-1]", "/foo[-1]/bar", IRRELEVANT, true},
            {"/foo[-1] is parent of /foo[0]/bar", "/foo[-1]", "/foo[0]/bar", IRRELEVANT, true},
            {"/foo[-1] is parent of /foo[2]/bar", "/foo[-1]", "/foo[2]/bar", IRRELEVANT, true},
            {"/foo/bar[-1] is parent of /foo/bar/baz", "/foo/bar[-1]", "/foo/bar/baz", IRRELEVANT, true},
            {"/foo/bar[-1] is parent of /foo/bar[-1]/baz", "/foo/bar[-1]", "/foo/bar[-1]/bzr", IRRELEVANT, true},
            {"/foo/bar[-1] is parent of /foo/bar[0]/baz", "/foo/bar[-1]", "/foo/bar[0]/bzr", IRRELEVANT, true},
            {"/foo/bar[-1] is parent of /foo/bar[2]/baz", "/foo/bar[-1]", "/foo/bar[2]/bzr", IRRELEVANT, true},

            // Multiplicity scenario: [0] ancestor is parent of other special multiplicities only at first level
            {"/foo[0] is parent of /foo/bar", "/foo[0]", "/foo/bar", IRRELEVANT, true},
            {"/foo[0] is parent of /foo[-1]/bar", "/foo[0]", "/foo[-1]/bar", IRRELEVANT, true},
            {"/foo[0] is parent of /foo[0]/bar", "/foo[0]", "/foo[0]/bar", IRRELEVANT, true},
            {"/foo[0] is not parent of /foo[2]/bar", "/foo[0]", "/foo[2]/bar", IRRELEVANT, false},
            {"/foo/bar[0] is not parent of /foo/bar/baz", "/foo/bar[0]", "/foo/bar/baz", IRRELEVANT, false},
            {"/foo/bar[0] is not parent of /foo/bar[-1]/baz", "/foo/bar[0]", "/foo/bar[-1]/baz", IRRELEVANT, false},
            {"/foo/bar[0] is parent of /foo/bar[0]/baz", "/foo/bar[0]", "/foo/bar[0]/baz", IRRELEVANT, true},
            {"/foo/bar[0] is not parent of /foo/bar[2]/baz", "/foo/bar[0]", "/foo/bar[2]/baz", IRRELEVANT, false},

            // Multiplicity scenario: [2] ancestor is not parent of special multiplicities, regardless of level
            {"/foo[2] is parent of /foo/bar", "/foo[2]", "/foo[0]/bar", IRRELEVANT, false},
            {"/foo[2] is parent of /foo[-1]/bar", "/foo[2]", "/foo[-1]/bar", IRRELEVANT, false},
            {"/foo[2] is parent of /foo[0]/bar", "/foo[2]", "/foo[0]/bar", IRRELEVANT, false},
            {"/foo[2] is parent of /foo[2]/bar", "/foo[2]", "/foo[2]/bar", IRRELEVANT, true},
            {"/foo/bar[2] is parent of /foo/bar/baz", "/foo/bar[2]", "/foo/bar[0]/baz", IRRELEVANT, false},
            {"/foo/bar[2] is parent of /foo/bar[-1]/baz", "/foo/bar[2]", "/foo/bar[-1]/baz", IRRELEVANT, false},
            {"/foo/bar[2] is parent of /foo/bar[0]/baz", "/foo/bar[2]", "/foo/bar[0]/baz", IRRELEVANT, false},
            {"/foo/bar[2] is parent of /foo/bar[2]/baz", "/foo/bar[2]", "/foo/bar[2]/baz", IRRELEVANT, true},

            // Multiplicity scenario: Mixed bag of other interesting examples
            {"/foo[-1]/bar[2] is parent of /foo/bar[2]/baz", "/foo[-1]/bar[2]", "/foo/bar[2]/baz", IRRELEVANT, true},
            {"/foo[-1]/bar[2] is parent of /foo[-1]/bar[2]/baz", "/foo[-1]/bar[2]", "/foo[-1]/bar[2]/baz", IRRELEVANT, true},
            {"/foo[-1]/bar[2] is parent of /foo[0]/bar[2]/baz", "/foo[-1]/bar[2]", "/foo[0]/bar[2]/baz", IRRELEVANT, true},
            {"/foo[-1]/bar[2] is parent of /foo[2]/bar[2]/baz", "/foo[-1]/bar[2]", "/foo[2]/bar[2]/baz", IRRELEVANT, true},
            {"/foo[2]/bar[2] is parent of /foo[3]/bar[2]/baz", "/foo[2]/bar[2]", "/foo[3]/bar[2]/baz", IRRELEVANT, false},
        });
    }

    @Test
    public void parent_works_as_expected() {
        assertThat(
            buildRef(a).isAncestorOf(buildRef(b), properParent),
            is(expectedResult)
        );
    }

}
