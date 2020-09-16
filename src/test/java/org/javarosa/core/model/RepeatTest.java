package org.javarosa.core.model;

import org.javarosa.core.test.Scenario;
import org.javarosa.form.api.FormEntryController;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.junit.Assert.assertThat;

public class RepeatTest {

    @Test
    public void whenRepeatIsNotRelevant_repeatPromptIsSkipped() throws Exception {
        Scenario scenario = Scenario.init("Non relevant repeat", html(
            head(
                title("Non relevant repeat"),
                model(
                    mainInstance(t("data id=\"non_relevant_repeat\"",
                        t("repeat1",
                            t("q1"))
                    )),
                    bind("/data/repeat1").relevant("false()")
                ),
                body(
                    repeat("/data/repeat1",
                        input("/data/repeat1/q1")
                    )
                ))));

        scenario.jumpToBeginningOfForm();

        int event = scenario.next();
        assertThat(event, is(FormEntryController.EVENT_END_OF_FORM));
    }
}
