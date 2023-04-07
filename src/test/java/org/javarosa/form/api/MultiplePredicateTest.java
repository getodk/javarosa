package org.javarosa.form.api;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

public class MultiplePredicateTest {

    @Test
    public void calculatesSupportMultiplePredicates() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc"),
                        t("input")
                    )),
                    instance("instance",
                        t("item",
                            t("value", "A")
                        ),
                        t("item",
                            t("value", "B")
                        ),
                        t("item",
                            t("value", "C")
                        )
                    ),
                    bind("/data/calc").type("string")
                        .calculate("instance('instance')/root/item[value != 'A'][value != 'C']/value"),
                    bind("/data/input").type("string")
                )
            ),
            body(input("/data/input"))
        ));

        assertThat(scenario.answerOf("/data/calc").getValue(), equalTo("B"));
    }
}
