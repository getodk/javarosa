package org.javarosa.core.model.test;

import org.javarosa.core.test.Scenario;
import org.javarosa.measure.Measure;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.instance;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class PredicateCachingTest {

    @Test
    public void repeatedEqPredicatesAreOnlyEvaluatedOnceWhileAnswering() throws Exception {
        Scenario scenario = Scenario.init("secondary-instance-filter.xml");

        int evaluations = Measure.withMeasure("PredicateEvaluations", () -> {
            scenario.answer("/data/choice", "a");
        });

        // Check that we do less than (size of secondary instance) * (number of calculates with a filter)
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void repeatedEqPredicatesAreOnlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("secondary-instance-filter.xml");

        int evaluations = Measure.withMeasure("PredicateEvaluations", () -> {
            scenario.answer("/data/choice", "a");
            scenario.answer("/data/choice", "a");
        });

        // Check that we do less than size of secondary instance * 2
        assertThat(evaluations, lessThan(4));
    }

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

    @Test
    public void predicatesOnDifferentChildNamesDoNotGetConfused() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("cat"),
                        t("dog"),
                        t("input")
                    )),
                    instance("instance",
                        t("cat",
                            t("name", "Vinnie"),
                            t("age", "12")
                        ),
                        t("dog",
                            t("name", "Vinnie"),
                            t("age", "9")
                        )
                    ),
                    bind("/data/cat").type("string")
                        .calculate("instance('instance')/root/cat[name = 'Vinnie']/age"),
                    bind("/data/dog").type("string")
                        .calculate("instance('instance')/root/dog[name = 'Vinnie']/age"),
                    bind("/data/input").type("string")
                )
            ),
            body(input("/data/input"))
        ));

        assertThat(scenario.answerOf("/data/cat").getValue(), equalTo("12"));
        assertThat(scenario.answerOf("/data/dog").getValue(), equalTo("9"));
    }
}
