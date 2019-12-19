package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.answerText;
import static org.javarosa.core.test.SelectChoiceMatchers.choice;

import org.javarosa.core.test.Scenario;
import org.junit.Before;
import org.junit.Test;

/**
 * When itemsets are dynamically generated, the choices available to a user in a select multiple question can change
 * based on the answers given to other questions. These tests verify that when several select multiples are chained in a
 * cascading pattern, updating selections at root levels correctly updates the choices available in dependent selects
 * all the way down the cascade.  They also verify that if an answer that is no longer part of the available choices was
 * previously selected, that selection is removed from the answer.
 *
 * Select ones use the same code paths so see also {@link SelectOneChoiceFilterTest} for more explicit cases at each level.
 */
public class SelectMultipleChoiceFilterTest {
    private Scenario scenario;

    @Before    public void setUp() {
        scenario = Scenario.init("three-level-cascading-multi-select.xml");
    }

    @Test public void dependentLevelsInBlankInstance_haveNoChoices() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());
    }

    @Test public void selectingValueAtLevel1_filtersChoicesAtLevel2() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());

        scenario.answer("/data/level1", "a", "b");

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

        scenario.answer("/data/level1", "a", "b");
        scenario.answer("/data/level2", "aa", "ba");
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("aaa"),
            choice("aab"),
            choice("baa"),
            choice("bab")));
    }

    @Test public void newChoiceFilterEvaluation_removesIrrelevantAnswersAtAllLevels_withoutChangingOrder() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "a", "b", "c");
        scenario.answer("/data/level2", "aa", "ba", "ca");
        scenario.answer("/data/level3", "aab", "baa", "aaa");

        // Remove b from the level1 answer; this should filter out b-related answers and choices at levels 2 and 3
        scenario.answer("/data/level1", "a", "c");

        // Force populateDynamicChoices to run again which is what filters out irrelevant answers
        scenario.choicesOf("/data/level2");

        assertThat(scenario.answerOf("/data/level2"), is(answerText("aa, ca")));

        // This also runs populateDynamicChoices and filters out irrelevant answers
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("aaa"),
            choice("aab"),
            choice("caa"),
            choice("cab")));

        assertThat(scenario.answerOf("/data/level3"), is(answerText("aab, aaa")));
    }

    @Test public void newChoiceFilterEvaluation_leavesAnswerUnchangedIfAllSelectionsStillInChoices() {
        scenario.newInstance();
        assertThat(scenario.choicesOf("/data/level2"), empty());
        assertThat(scenario.choicesOf("/data/level3"), empty());

        scenario.answer("/data/level1", "a", "b", "c");
        scenario.answer("/data/level2", "aa", "ba", "bb", "ab");
        scenario.answer("/data/level3", "aab", "baa", "aaa");

        // Remove c from the level1 answer; this should have no effect on levels 2 and 3
        scenario.answer("/data/level1", "a", "b");

        // Force populateDynamicChoices to run again which is what filters out irrelevant answers
        scenario.choicesOf("/data/level2");

        assertThat(scenario.answerOf("/data/level2"), is(answerText("aa, ba, bb, ab")));

        // This also runs populateDynamicChoices and filters out irrelevant answers
        assertThat(scenario.choicesOf("/data/level3"), containsInAnyOrder(
            choice("aaa"),
            choice("aab"),
            choice("baa"),
            choice("bab")));

        assertThat(scenario.answerOf("/data/level3"), is(answerText("aab, baa, aaa")));
    }
}
