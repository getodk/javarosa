package org.javarosa.core.model;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.select1;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.junit.Assert.assertThat;

import org.javarosa.core.test.Scenario;
import org.javarosa.form.api.FormEntryController;
import org.junit.Test;

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
                )),
            body(
                repeat("/data/repeat1",
                    input("/data/repeat1/q1")
                )
            )));

        scenario.jumpToBeginningOfForm();

        int event = scenario.next();
        assertThat(event, is(FormEntryController.EVENT_END_OF_FORM));
    }

    @Test
    public void whenRepeatRelevanceIsDynamic_andNotRelevant_repeatPromptIsSkipped() throws Exception {
        Scenario scenario = Scenario.init("Repeat relevance - dynamic expression", html(
            head(
                title("Repeat relevance - dynamic expression"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_dynamic\"",
                        t("selectYesNo"),
                        t("repeat1",
                            t("q1"))
                    )),
                    bind("/data/repeat1").relevant("/data/selectYesNo = 'yes'")
                )),
            body(
                select1("/data/selectYesNo",
                    item("yes", "Yes"),
                    item("no", "No")),
                repeat("/data/repeat1",
                    input("/data/repeat1/q1")
                )
            )));

        scenario.jumpToBeginningOfForm();
        scenario.answer("/data/selectYesNo", "no");

        int event = scenario.next();
        assertThat(event, is(FormEntryController.EVENT_END_OF_FORM));
    }

    @Test
    public void whenRepeatAndTopLevelNodeHaveSameRelevanceExpression_andExpressionEvaluatesToFalse_repeatPromptIsSkipped() throws Exception {
        Scenario scenario = Scenario.init("Repeat relevance same as other", html(
            head(
                title("Repeat relevance same as other"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_same_as_other\"",
                        t("selectYesNo", "no"),
                        t("repeat1",
                            t("q1")),
                        t("q0")
                    )),
                    bind("/data/q0").relevant("/data/selectYesNo = 'yes'"),
                    bind("/data/repeat1").relevant("/data/selectYesNo = 'yes'")
                )),
            body(
                select1("/data/selectYesNo",
                    item("yes", "Yes"),
                    item("no", "No")),
                repeat("/data/repeat1",
                    input("/data/repeat1/q1")
                )
            )));

        scenario.jumpToBeginningOfForm();
        scenario.next();
        int event = scenario.next();

        assertThat(event, is(FormEntryController.EVENT_END_OF_FORM));
    }
}
