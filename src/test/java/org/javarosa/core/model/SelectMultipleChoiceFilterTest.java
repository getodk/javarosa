package org.javarosa.core.model;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.test.Scenario;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.answerText;
import static org.javarosa.core.test.SelectChoiceMatchers.choice;

/**
 * When itemsets are dynamically generated, the choices available to a user in a select multiple question can change
 * based on the answers given to other questions. These tests verify that when several select multiples are chained in a
 * cascading pattern, updating selections at root levels correctly updates the choices available in dependent selects
 * all the way down the cascade.  They also verify that if an answer that is no longer part of the available choices was
 * previously selected, that selection is removed from the answer.
 */
public class SelectMultipleChoiceFilterTest {
    private Scenario scenario;

    @Before public void setUp() {
        scenario = Scenario.init("three-level-cascading-multi-select.xml");
    }

    @Test public void dependentLevelsInBlankInstance_haveNoChoices() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());
    }

    /**
     * Level 1 is a static choice list.
     */
    @Test public void selectingValueAtLevel1_filtersChoicesAtLevel2() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());

        scenario.answer("/data/level1", Arrays.asList(new Selection("a"), new Selection("b")));

        assertThat(scenario.choicesOf("/data/level2"), containsInAnyOrder(
            choice("aa"),
            choice("ab"),
            choice("ac"),
            choice("ba"),
            choice("bb"),
            choice("bc")));
    }

    @Test public void selectingValuesAtLevels1And2_filtersChoicesAtLevel3() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", Arrays.asList(new Selection("a"), new Selection("b")));
        scenario.answer("/data/level2", Arrays.asList(new Selection("aa"), new Selection("ba")));
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("aaa"),
            choice("aab"),
            choice("baa"),
            choice("bab")));

        // Force populateDynamicChoices to run again
        scenario.choicesOf("/data/level2");

        assertThat(scenario.answerOf("/data/level2"), is(answerText("aa, ba")));
    }

    @Test public void newChoiceFilterEvaluation_removesIrrelevantAnswersAtAllLevels_withoutChangingOrder() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", Arrays.asList(new Selection("a"), new Selection("b"), new Selection("c")));
        scenario.answer("/data/level2", Arrays.asList(new Selection("aa"), new Selection("ba"), new Selection("ca")));
        scenario.answer("/data/level3", Arrays.asList(new Selection("aab"), new Selection("baa"), new Selection("aaa")));

        // Remove b from the level1 answer
        scenario.answer("/data/level1", Arrays.asList(new Selection("a"), new Selection("c")));

        // Force populateDynamicChoices to run again
        scenario.choicesOf("/data/level2");

        assertThat(scenario.answerOf("/data/level2"), is(answerText("aa, ca")));

        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("aaa"),
            choice("aab")));

        assertThat(scenario.answerOf("/data/level3"), is(answerText("aab, aaa")));

    }
}
