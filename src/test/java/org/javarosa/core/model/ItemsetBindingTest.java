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

package org.javarosa.core.model;

import org.javarosa.core.test.Scenario;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.test.SelectChoiceMatchers.choice;

public class ItemsetBindingTest {
    private Scenario scenario;

    @Before
    public void setUp() {
        scenario = Scenario.init("three-level-cascading-select.xml");
    }

    @Test
    public void dependentLevelsInBlankInstance_ShouldHaveNoChoices() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());
    }

    @Test
    public void selectingValueAtLevel1_ShouldFilterChoicesAtLevel2() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());

        scenario.answer("/data/level1", "b");

        assertThat(scenario.choicesOf("/data/level2"), containsInAnyOrder(
            choice("b1"),
            choice("b2"),
            choice("b3")));
    }

    @Test
    public void selectingValuesAtLevels1And2_ShouldFilterChoicesAtLevel3() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "b");
        scenario.answer("/data/level2", "b1");
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("b1a"),
            choice("b1b")));
    }

    @Test
    public void clearingValueAtLevel2_ShouldClearChoicesAtLevel3() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "a");
        scenario.answer("/data/level2", "a1");
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("a1a"),
            choice("a1b")));
        scenario.answer("/data/level2", "");
        assertThat(scenario.choicesOf("/data/level3"), empty());
    }

    @Test
    public void clearingValueAtLevel1_ShouldClearChoicesAtLevels2And3() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "a");
        scenario.answer("/data/level2", "a1");
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("a1a"),
            choice("a1b")));

        scenario.answer("/data/level1", "");
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.answerOf("/data/level2"), is(stringAnswer("")));
        assertThat(scenario.choicesOf("/data/level3"), empty());
    }
}
