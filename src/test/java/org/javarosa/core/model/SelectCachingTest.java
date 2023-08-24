package org.javarosa.core.model;

import org.javarosa.core.test.Scenario;
import org.javarosa.measure.Measure;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.instance;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.select1Dynamic;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class SelectCachingTest {

    @Test
    public void eqChoiceFiltersAreOnlyEvaluatedOnceForRepeatedChoiceListEvaluations() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select", "instance('instance')/root/item[value=/data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");

            scenario.choicesOf("/data/select");
            scenario.choicesOf("/data/select");
        });

        // Check that we do less than (size of secondary instance) * (number of choice lookups)
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void repeatedEqChoiceFiltersAreOnlyEvaluatedOnce_whileLiteralExpressionIsTheSame() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select1"),
                        t("select2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select1").type("string"),
                    bind("/data/select2").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select1", "instance('instance')/root/item[value=/data/choice]"),
                select1Dynamic("/data/select2", "instance('instance')/root/item[value=/data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");

            scenario.choicesOf("/data/select1");
            scenario.choicesOf("/data/select2");
        });

        // Check that we do less than (size of secondary instance) * (number of choice lookups)
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void repeatedCompChoiceFiltersAreOnlyEvaluatedOnce_whileLiteralExpressionIsTheSame() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select1"),
                        t("select2")
                    )),
                    instance("instance",
                        item("1", "A"),
                        item("2", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select1").type("string"),
                    bind("/data/select2").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select1", "instance('instance')/root/item[value</data/choice]"),
                select1Dynamic("/data/select2", "instance('instance')/root/item[value</data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "3");

            scenario.choicesOf("/data/select1");
            scenario.choicesOf("/data/select2");
        });

        // Check that we do less than (size of secondary instance) * (number of choice lookups)
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void repeatedEqChoiceFiltersAreOnlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select1"),
                        t("select2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select1").type("string"),
                    bind("/data/select2").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select1", "instance('instance')/root/item[value=/data/choice]"),
                select1Dynamic("/data/select2", "instance('instance')/root/item[value=/data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");
            scenario.choicesOf("/data/select1");

            scenario.answer("/data/choice", "b");
            scenario.choicesOf("/data/select2");
        });

        // Check that we do less than (size of secondary instance) * (number of choice lookups)
        assertThat(evaluations, lessThan(4));
    }
}
