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
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.test.Scenario.getRef;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TreeReferenceContextualizeTest {
    @Parameterized.Parameter(value = 0)
    public String testCase;

    @Parameterized.Parameter(value = 1)
    public TreeReference a;

    @Parameterized.Parameter(value = 2)
    public TreeReference b;

    @Parameterized.Parameter(value = 3)
    public TreeReference expectedResult;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"Relative context ref", getRef("bar"), getRef("foo"), null},
            {"Non-matching absolute refs", getRef("/foo"), getRef("/bar"), getRef("/foo")},

            {"Contextualizing a ref with itself and multiplicity #1", getRef("/foo"), getRef("/foo[2]"), getRef("/foo[2]")},
            {"Contextualizing a ref with itself and multiplicity #2", getRef("/foo[-1]"), getRef("/foo[2]"), getRef("/foo[2]")},
            {"Contextualizing a ref with itself and multiplicity #3", getRef("/foo[0]"), getRef("/foo[2]"), getRef("/foo[2]")},
            {"Contextualizing a ref with itself and multiplicity #4", getRef("/foo[3]"), getRef("/foo[2]"), getRef("/foo[2]")},

            {"Contextualizing a ref with itself and predicate #1", getRef("/foo"), getRef("/foo[position() = 3]"), getRef("/foo[position() = 3]")},
            {"Contextualizing a ref with itself and predicate #2", getRef("/foo[position() = 2]"), getRef("/foo[position() = 3]"), getRef("/foo[position() = 2]")},

            {"Contextualizing a ref with predicate with itself and multiplicity", getRef("/foo[position() = 2]"), getRef("/foo[1]"), getRef("/foo[position() = 2]")},
            {"Contextualizing a ref with multiplicity with itself and predicate", getRef("/foo[1]"), getRef("/foo[position() = 2]"), getRef("/foo[position() = 2]")},

            {"Wildcards #1", getRef("bar"), getRef("/foo/*"), getRef("/foo/*/bar")},
            {"Wildcards #2", getRef("../baz"), getRef("/foo/*/bar"), getRef("/foo/*/baz")},
            {"Wildcards #3", getRef("*/baz"), getRef("/foo/*/bar"), getRef("/foo/*/bar/*/baz")},
            {"Wildcards #4", getRef("*/bar"), getRef("/foo"), getRef("/foo/*/bar")},

            {"Predicates #1", getRef("bar"), getRef("/foo[@foo = 'bar']"), getRef("/foo[@foo = 'bar']/bar")},
            {"Predicates #2", getRef("bar[@foo = 'baz']"), getRef("/foo[@foo = 'bar']"), getRef("/foo[@foo = 'bar']/bar[@foo = 'baz']")},
            {"Predicates #3", getRef("bar[@foo = 'baz']"), getRef("/foo"), getRef("/foo/bar[@foo = 'baz']")},
            {"Predicates #4", getRef("/foo[@foo = 'foo']"), getRef("/foo[@foo = 'bar']"), getRef("/foo[@foo = 'foo']")},

            {"Parent refs in nested repeats", getRef("../../inner"), getRef("/data/outer[0]/inner[1]/count[0]"), getRef("/data/outer[0]/inner")},
            {"Parent refs in child of repeat", getRef("../../repeat/value"), getRef("/data/repeat[0]/sum[0]"), getRef("/data/repeat/value")},
            {"Parent refs in child of repeat", getRef("../../../outer/inner/bar/baz"), getRef("/data/outer[17]/inner[5]/foo"), getRef("/data/outer/inner/bar/baz")}
        });
    }


    @Test
    public void contextualize() {
        assertThat(
            a.contextualize(b),
            expectedResult == null
                ? nullValue()
                : is(expectedResult)
        );
    }
}
