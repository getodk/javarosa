package org.javarosa.core.model.actions;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.setvalue;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.hamcrest.CoreMatchers;
import org.javarosa.core.test.Scenario;
import org.junit.Test;

public class InstanceLoadEventsTest {
    @Test
    public void instanceLoadEvent_firesOnFirstLoad() throws Exception {
        Scenario scenario = Scenario.init("Instance load form", html(
            head(
                title("Instance load form"),
                model(
                    mainInstance(
                        t("data id=\"instance-load-form\"",
                            t("q1")
                        )),
                    bind("/data/q1").type("int"),
                    setvalue("odk-instance-load", "/data/q1", "4*4"))),
            body(
                input("/data/q1")
            )
        ));

        assertThat(scenario.answerOf("/data/q1"), is(intAnswer(16)));
    }

    @Test
    public void instanceLoadEvent_firesOnSecondLoad() throws Exception {
        Scenario scenario = Scenario.init("Instance load form", html(
            head(
                title("Instance load form"),
                model(
                    mainInstance(
                        t("data id=\"instance-load-form\"",
                            t("q1")
                        )),
                    bind("/data/q1").type("int"),
                    setvalue("odk-instance-load", "/data/q1", "4*4"))),
            body(
                input("/data/q1")
            )
        ));

        assertThat(scenario.answerOf("/data/q1"), is(intAnswer(16)));
        scenario.answer("/data/q1", 555);

        Scenario restored = scenario.serializeAndDeserializeForm();
        assertThat(restored.answerOf("/data/q1"), is(intAnswer(16)));
    }

    @Test
    public void instanceFirstLoadEvent_doesNotfireOnSecondLoad() throws Exception {
        Scenario scenario = Scenario.init("Instance load form", html(
            head(
                title("Instance load form"),
                model(
                    mainInstance(
                        t("data id=\"instance-load-form\"",
                            t("q1")
                        )),
                    bind("/data/q1").type("int"),
                    setvalue("odk-instance-first-load", "/data/q1", "4*4"))),
            body(
                input("/data/q1")
            )
        ));

        assertThat(scenario.answerOf("/data/q1"), is(intAnswer(16)));
        scenario.answer("/data/q1", 555);

        Scenario restored = scenario.serializeAndDeserializeForm();
        assertThat(restored.answerOf("/data/q1"), is(intAnswer(555)));
    }

    @Test
    public void instanceLoadEvent_triggersNestedActions() throws IOException {
        Scenario scenario = Scenario.init("Nested instance load", html(
            head(
                title("Nested instance load"),
                model(
                    mainInstance(
                        t("data id=\"nested-instance-load\"",
                            t("repeat",
                                t("q1"))
                        )),
                    bind("/data/repeat/q1").type("string"))),
            body(
                repeat("/data/repeat",
                    setvalue("odk-instance-load", "/data/repeat/q1", "4*4"),
                    input("/data/repeat/q1"))
            )
        ));

        assertThat(scenario.answerOf("/data/repeat[0]/q1"), CoreMatchers.is(stringAnswer("16")));

        scenario.createNewRepeat("/data/repeat");
        assertThat(scenario.answerOf("/data/repeat[1]/q1"), CoreMatchers.is(nullValue()));
    }
}
