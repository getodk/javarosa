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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.test.SelectChoiceMatchers.choice;
import static org.javarosa.form.api.FormEntryController.ANSWER_REQUIRED_BUT_EMPTY;

import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * When itemsets are dynamically generated, the choices available to a user in a select one question can change based on
 * the answers given to other questions. These tests verify that when several select ones are chained in a cascading
 * pattern, updating selections at root levels correctly updates the choices available in dependent selects all the way
 * down the cascade. They also verify that if an answer that is no longer part of the available choices was previously
 * selected, that answer is cleared.
 */
public class SelectOneChoiceFilterTest {
    private Scenario scenario;

    @Before
    public void setUp() throws ParseException {
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
            choice("ba"),
            choice("bb"),
            choice("bc")));
    }

    @Test
    public void selectingValuesAtLevels1And2_ShouldFilterChoicesAtLevel3() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "b");
        scenario.answer("/data/level2", "ba");
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("baa"),
            choice("bab")));
    }

    @Test
    public void clearingValueAtLevel2_ShouldClearChoicesAtLevel3() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "a");
        scenario.answer("/data/level2", "aa");
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("aaa"),
            choice("aab")));
        scenario.answer("/data/level2", "");
        assertThat(scenario.choicesOf("/data/level3"), empty());
    }

    @Test
    public void clearingValueAtLevel1_ShouldClearChoicesAtLevels2And3() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "a");
        scenario.answer("/data/level2", "aa");
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("aaa"),
            choice("aab")));

        scenario.answer("/data/level1", "");
        assertThat(scenario.choicesOf("/data/level2"), empty());
        // this next assertion is only true because the one before called populateDynamic choices
        // TODO: make clearing out answers that are no longer available choices part of the form re-evaluation
        assertThat(scenario.answerOf("/data/level2"), nullValue());
        assertThat(scenario.choicesOf("/data/level3"), empty());
    }

    @Test
    public void clearingValueAtLevel1_ShouldClearValuesAtLevels2And3() {
        scenario.newInstance();
        assertThat(scenario.answerOf("/data/level2"), nullValue());
        assertThat(scenario.answerOf("/data/level3"), nullValue());

        scenario.answer("/data/level1", "a");
        scenario.answer("/data/level2", "aa");
        scenario.answer("/data/level3", "aab");

        scenario.answer("/data/level1", "");

        ValidateOutcome validate = scenario.getValidationOutcome();
        Assert.assertThat(validate.failedPrompt, is(scenario.indexOf("/data/level2")));
        Assert.assertThat(validate.outcome, is(ANSWER_REQUIRED_BUT_EMPTY));

        // If we set level2 to "aa", form validation passes. Currently, clearing a choice only updates filter expressions
        // that directly depend on it. With this form, we could force clearing the third level when the first level is cleared
        // by making the level3 filter expression in the form definition reference level1 AND level2.
        scenario.answer("/data/level1", "b");
        scenario.answer("/data/level2", "bb");

        validate = scenario.getValidationOutcome();
        Assert.assertThat(validate.failedPrompt, is(scenario.indexOf("/data/level3")));
        Assert.assertThat(validate.outcome, is(ANSWER_REQUIRED_BUT_EMPTY));
    }

    @Test
    public void changingValueAtLevel2_ShouldClearLevel3_IfChoiceNoLongerAvailable() {
        scenario.newInstance();

        scenario.answer("/data/level1_contains", "a");
        scenario.answer("/data/level2_contains", "aa");
        assertThat(scenario.choicesOf("/data/level3_contains"), containsInAnyOrder(
            choice("aaa"),
            choice("aab"),
            choice("baa")));
        scenario.answer("/data/level3_contains", "aaa");
        scenario.answer("/data/level2_contains", "ab");
        assertThat(scenario.choicesOf("/data/level3_contains"), containsInAnyOrder(
            choice("aab"),
            choice("bab")));
        // this next assertion is only true because the one before called populateDynamicChoices
        // TODO: make clearing out answers that are no longer available choices part of the form re-evaluation
        assertThat(scenario.answerOf("/data/level3_contains"), nullValue());
    }

    @Test
    public void changingValueAtLevel2_ShouldNotClearLevel3_IfChoiceStillAvailable() {
        scenario.newInstance();

        scenario.answer("/data/level1_contains", "a");
        scenario.answer("/data/level2_contains", "aa");
        assertThat(scenario.choicesOf("/data/level3_contains"), containsInAnyOrder(
            choice("aaa"),
            choice("aab"),
            choice("baa")));
        scenario.answer("/data/level3_contains", "aab");
        scenario.answer("/data/level2_contains", "ab");
        assertThat(scenario.answerOf("/data/level3_contains").getDisplayText(), is("aab"));

        // Since recomputing the choice list can change answers, verify it doesn't in this case
        assertThat(scenario.choicesOf("/data/level3_contains"), containsInAnyOrder(
            choice("aab"),
            choice("bab")));
        assertThat(scenario.answerOf("/data/level3_contains").getDisplayText(), is("aab"));
    }
}
