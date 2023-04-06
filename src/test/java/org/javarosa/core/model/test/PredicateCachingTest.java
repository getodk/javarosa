package org.javarosa.core.model.test;

import org.javarosa.core.test.Scenario;
import org.javarosa.measure.Measure;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PredicateCachingTest {

    @Test
    public void repeatedPredicatesAreOnlyEvaluatedOnceWhileAnswering() throws Exception {
        Scenario scenario = Scenario.init("secondary-instance-filter.xml");

        int evaluations = Measure.withMeasure("XPathEqExpr#eval", new Runnable() {
            @Override
            public void run() {
                scenario.answer("/data/choice","a");
            }
        });

        assertThat(evaluations, equalTo(2));
    }
}
