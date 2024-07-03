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
            {"/foo is not ancestor of /foo if we exclude self references", "/foo", "/foo", true, false},
            {"/foo/bar is not ancestor of /foo/bar if we exclude self references", "/foo/bar", "/foo/bar", true, false},
            {"/foo is ancestor of /foo if we don't exclude self references", "/foo", "/foo", false, true},
            {"/foo/bar is  ancestor of /foo/bar if we don't exclude self references", "/foo/bar", "/foo/bar", false, true},

            // Simple scenarios
            {"/foo/bar is not ancestor of /foo", "/foo/bar", "/foo", IRRELEVANT, false},
            {"/foo is not ancestor of /bar", "/foo", "/bar", IRRELEVANT, false},
            {"foo is not ancestor of bar", "foo", "bar", IRRELEVANT, false},
            {"/foo is not ancestor of /bar/foo", "/foo", "/bar/foo", IRRELEVANT, false},
            {"/foo is ancestor of /foo/bar", "/foo", "/foo/bar", IRRELEVANT, true},
            {"/foo/bar is ancestor of /foo/bar/baz", "/foo/bar", "/foo/bar/baz", IRRELEVANT, true},

            // Ancestry (more than ancestor) scenarios
            {"/foo is ancestor of /foo/bar/baz", "/foo", "/foo/bar/baz", IRRELEVANT, true},

            // Scenarios with a mix of relative and absolute paths
            {"foo is not ancestor of /foo/bar", "foo", "/foo/bar", IRRELEVANT, false},
            {"/foo is not ancestor of foo/bar", "/foo", "foo/bar", IRRELEVANT, false},

            // We will consider undefined (no multiplicity predicate), [-1], and [0] multiplicities due to their special meaning and [2]
            // as an example of a specific non-special multiplicity
            // We will also consider first-level and following-level multiplicities, because the first level behaves differently

            // Multiplicity scenario: undefined ancestor is ancestor of any other multiplicity, regardless of level
            {"/foo is ancestor of /foo/bar", "/foo", "/foo/bar", IRRELEVANT, true},
            {"/foo is ancestor of /foo[-1]/bar", "/foo", "/foo[-1]/bar", IRRELEVANT, true},
            {"/foo is ancestor of /foo[1]/bar", "/foo", "/foo[1]/bar", IRRELEVANT, true},
            {"/foo is ancestor of /foo[3]/bar", "/foo", "/foo[3]/bar", IRRELEVANT, true},
            {"/foo/bar is ancestor of /foo/bar/baz", "/foo/bar", "/foo/bar/baz", IRRELEVANT, true},
            {"/foo/bar is ancestor of /foo/bar[-1]/baz", "/foo/bar", "/foo/bar[-1]/baz", IRRELEVANT, true},
            {"/foo/bar is ancestor of /foo/bar[1]/baz", "/foo/bar", "/foo/bar[1]/baz", IRRELEVANT, true},
            {"/foo/bar is ancestor of /foo/bar[3]/baz", "/foo/bar", "/foo/bar[3]/baz", IRRELEVANT, true},

            // Multiplicity scenario: [-1] ancestor is ancestor of any other multiplity, regardless of level
            {"/foo[-1] is ancestor of /foo/bar", "/foo[-1]", "/foo/bar", IRRELEVANT, true},
            {"/foo[-1] is ancestor of /foo[-1]/bar", "/foo[-1]", "/foo[-1]/bar", IRRELEVANT, true},
            {"/foo[-1] is ancestor of /foo[1]/bar", "/foo[-1]", "/foo[1]/bar", IRRELEVANT, true},
            {"/foo[-1] is ancestor of /foo[3]/bar", "/foo[-1]", "/foo[3]/bar", IRRELEVANT, true},
            {"/foo/bar[-1] is ancestor of /foo/bar/baz", "/foo/bar[-1]", "/foo/bar/baz", IRRELEVANT, true},
            {"/foo/bar[-1] is ancestor of /foo/bar[-1]/baz", "/foo/bar[-1]", "/foo/bar[-1]/bzr", IRRELEVANT, true},
            {"/foo/bar[-1] is ancestor of /foo/bar[1]/baz", "/foo/bar[-1]", "/foo/bar[1]/bzr", IRRELEVANT, true},
            {"/foo/bar[-1] is ancestor of /foo/bar[3]/baz", "/foo/bar[-1]", "/foo/bar[3]/bzr", IRRELEVANT, true},

            // Multiplicity scenario: [0] ancestor is ancestor of other special multiplicities only at first level
            {"/foo[0] is ancestor of /foo/bar", "/foo[1]", "/foo/bar", IRRELEVANT, true},
            {"/foo[0] is ancestor of /foo[-1]/bar", "/foo[1]", "/foo[-1]/bar", IRRELEVANT, true},
            {"/foo[0] is ancestor of /foo[1]/bar", "/foo[1]", "/foo[1]/bar", IRRELEVANT, true},
            {"/foo[0] is not ancestor of /foo[3]/bar", "/foo[1]", "/foo[3]/bar", IRRELEVANT, false},
            {"/foo/bar[0] is not ancestor of /foo/bar/baz", "/foo/bar[1]", "/foo/bar/baz", IRRELEVANT, false},
            {"/foo/bar[0] is not ancestor of /foo/bar[-1]/baz", "/foo/bar[1]", "/foo/bar[-1]/baz", IRRELEVANT, false},
            {"/foo/bar[0] is ancestor of /foo/bar[1]/baz", "/foo/bar[1]", "/foo/bar[1]/baz", IRRELEVANT, true},
            {"/foo/bar[0] is not ancestor of /foo/bar[3]/baz", "/foo/bar[1]", "/foo/bar[3]/baz", IRRELEVANT, false},

            // Multiplicity scenario: [2] ancestor is not ancestor of special multiplicities, regardless of level
            {"/foo[2] is ancestor of /foo/bar", "/foo[3]", "/foo[1]/bar", IRRELEVANT, false},
            {"/foo[2] is ancestor of /foo[-1]/bar", "/foo[3]", "/foo[-1]/bar", IRRELEVANT, false},
            {"/foo[2] is ancestor of /foo[1]/bar", "/foo[3]", "/foo[1]/bar", IRRELEVANT, false},
            {"/foo[2] is ancestor of /foo[3]/bar", "/foo[3]", "/foo[3]/bar", IRRELEVANT, true},
            {"/foo/bar[2] is ancestor of /foo/bar/baz", "/foo/bar[3]", "/foo/bar[1]/baz", IRRELEVANT, false},
            {"/foo/bar[2] is ancestor of /foo/bar[-1]/baz", "/foo/bar[3]", "/foo/bar[-1]/baz", IRRELEVANT, false},
            {"/foo/bar[2] is ancestor of /foo/bar[1]/baz", "/foo/bar[3]", "/foo/bar[1]/baz", IRRELEVANT, false},
            {"/foo/bar[2] is ancestor of /foo/bar[3]/baz", "/foo/bar[3]", "/foo/bar[3]/baz", IRRELEVANT, true},

            // Multiplicity scenario: Mixed bag of other interesting examples
            {"/foo[-1]/bar[2] is ancestor of /foo/bar[3]/baz", "/foo[-1]/bar[3]", "/foo/bar[3]/baz", IRRELEVANT, true},
            {"/foo[-1]/bar[2] is ancestor of /foo[-1]/bar[3]/baz", "/foo[-1]/bar[3]", "/foo[-1]/bar[3]/baz", IRRELEVANT, true},
            {"/foo[-1]/bar[2] is ancestor of /foo[1]/bar[3]/baz", "/foo[-1]/bar[3]", "/foo[1]/bar[3]/baz", IRRELEVANT, true},
            {"/foo[-1]/bar[2] is ancestor of /foo[3]/bar[3]/baz", "/foo[-1]/bar[3]", "/foo[3]/bar[3]/baz", IRRELEVANT, true},
            {"/foo[2]/bar[2] is ancestor of /foo[4]/bar[3]/baz", "/foo[3]/bar[3]", "/foo[4]/bar[3]/baz", IRRELEVANT, false},
        });
    }

    @Test
    public void isAncestorOf_works_as_expected() {
        assertThat(
            getRef(a).isAncestorOf(getRef(b), properParent),
            is(expectedResult)
        );
    }

}
