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
    public void EqChoiceFiltersAreOnlyEvaluatedOnceForRepeatedChoiceListEvaluations() throws Exception {
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
}
