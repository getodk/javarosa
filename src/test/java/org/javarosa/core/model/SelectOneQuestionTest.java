package org.javarosa.core.model;

import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.test.Scenario;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.instance;
import static org.javarosa.test.XFormsElement.item;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.select1Dynamic;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

public class SelectOneQuestionTest {

    @Test
    public void choiceIsSelectedWhenLiteralStringValueMatchesChoiceValue() throws IOException {
        Scenario scenario = Scenario.init(html(
            head(
                title("String calculate"),
                model(
                    mainInstance(t("data id=\"string-calculate\"",
                        t("select")
                    )),

                    instance("yes_no",
                        item(0, "No"),
                        item(1, "Yes")
                    ),
                    bind("/data/select").type("string").calculate("if(1=2, '1', '0')")
                )),
            body(
                select1Dynamic("/data/select", "instance('yes_no')/root/item")
            )
        ));

        scenario.choicesOf("/data/select"); // Populate choices
        assertThat(scenario.answerOf("/data/select").getValue(), equalTo(scenario.choicesOf("/data/select").get(0).selection()));
        assertThat(scenario.answerOf("/data/select"), is(instanceOf(SelectOneData.class)));
    }

    @Test
    public void choiceIsSelectedWhenLiteralIntegerValueMatchesChoiceValue() throws IOException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Integer calculate"),
                model(
                    mainInstance(t("data id=\"integer-calculate\"",
                        t("select")
                    )),

                    instance("yes_no",
                        item(0, "No"),
                        item(1, "Yes")
                    ),
                    bind("/data/select").type("string").calculate("if(1=2, 1, 0)")
                )),
            body(
                select1Dynamic("/data/select", "instance('yes_no')/root/item")
            )
        ));

        scenario.choicesOf("/data/select"); // Populate choices
        assertThat(scenario.answerOf("/data/select").getValue(), equalTo(scenario.choicesOf("/data/select").get(0).selection()));
        assertThat(scenario.answerOf("/data/select"), is(instanceOf(SelectOneData.class)));
    }

    @Test
    public void choiceIsSelectedWhenLiteralIntegerValueMatchesChoiceValue_afterDeserialization() throws IOException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Integer calculate"),
                model(
                    mainInstance(t("data id=\"integer-calculate\"",
                        t("select")
                    )),

                    instance("yes_no",
                        item(0, "No"),
                        item(1, "Yes")
                    ),
                    bind("/data/select").type("string").calculate("if(1=2, 1, 0)")
                )),
            body(
                select1Dynamic("/data/select", "instance('yes_no')/root/item")
            )
        )).serializeAndDeserializeForm();

        scenario.choicesOf("/data/select"); // Populate choices
        assertThat(scenario.answerOf("/data/select").getValue(), equalTo(scenario.choicesOf("/data/select").get(0).selection()));
        assertThat(scenario.answerOf("/data/select"), is(instanceOf(SelectOneData.class)));
    }

    @Test
    public void selectQuestionValueBlankWhenValueNotInChoices() throws IOException {
        Scenario scenario = Scenario.init(html(
            head(
                title("String calculate"),
                model(
                    mainInstance(t("data id=\"string-calculate\"",
                        t("select")
                    )),

                    instance("yes_no",
                        item(0, "No"),
                        item(1, "Yes")
                    ),
                    bind("/data/select").type("string").calculate("if(1=2, '1', '7')")
                )),
            body(
                select1Dynamic("/data/select", "instance('yes_no')/root/item")
            )
        ));

        scenario.choicesOf("/data/select"); // Populate choices
        assertThat(scenario.answerOf("/data/select"), nullValue());
    }
}
