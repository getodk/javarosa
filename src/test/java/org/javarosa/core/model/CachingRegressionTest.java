package org.javarosa.core.model;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CachingRegressionTest {

    /**
     * A form with multiple secondary instances can have expressions with "equivalent" predicates that filter on
     * different sets of children. It's pretty possible to write a bug where these predicates are treated as the same
     * thing causing incorrect answers.
     */
    @Test
    public void equivalentPredicateExpressionsOnDifferentReferencesAreNotConfused() throws Exception {
        Scenario scenario = Scenario.init("two-secondary-instances.xml");

        scenario.next();
        scenario.answer("a");
        assertThat(scenario.answerOf("/data/both").getValue(), equalTo("AA"));
    }

    @Test
    public void equivalentPredicateExpressionsInRepeatsDoNotGetConfused() throws Exception {
        Scenario scenario = Scenario.init("repeat-secondary-instance.xml");

        scenario.createNewRepeat("/data/repeat");
        scenario.createNewRepeat("/data/repeat");

        scenario.answer("/data/repeat[0]/choice", "a");
        assertThat(scenario.answerOf("/data/repeat[0]/calculate").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/repeat[1]/calculate"), equalTo(null));

        scenario.answer("/data/repeat[1]/choice", "b");
        assertThat(scenario.answerOf("/data/repeat[0]/calculate").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/repeat[1]/calculate").getValue(), equalTo("B"));
    }
}
