package org.javarosa.core.model;

import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.*;

public class SelectOneQuestionTest {

    @Test
    public void intAnswerIsCoercedToString() throws Exception {
        Scenario scenario = Scenario.init("Int Coercion", html(
            head(
                title("Int Coercion"),
                model(
                    mainInstance(t("data id=\"int-coercion\"",
                        t("select")
                    )),

                    instance("yes_no",
                        item(0, "No"),
                        item(1, "Yes")
                    ),
                    bind("/data/select").type("string")
                )),
            body(
                select1Dynamic("/data/select", "instance('yes_no')/root/item")
            )
        ));

        scenario.answer("/data/select", 0);
        scenario.choicesOf("/data/select"); // Clients need to call this first for coercion to work
        assertThat(scenario.answerOf("/data/select"), is(instanceOf(SelectOneData.class)));
        assertThat(scenario.answerOf("/data/select").getValue(), equalTo(scenario.choicesOf("/data/select").get(0).selection()));
    }
}
