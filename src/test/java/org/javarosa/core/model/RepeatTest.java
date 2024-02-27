package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
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

import java.io.IOException;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.test.Scenario;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
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

    /**
     * The original ODK XForms spec deviated from XPath rules by stating that path expressions representing single nodes
     * should be evaluated as relative to the current nodeset. That has since been removed and all known form builders
     * create relative references in expressions within a repeat. We currently maintain this behavior for legacy purposes.
     */
    @Test
    public void absoluteSingleNodePaths_areQualified_forLegacyPurposes() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Absolute relative ref", html(
            head(
                title("Absolute relative ref"),
                model(
                    mainInstance(t("data id=\"data\"",
                        t("outer",
                            t("outerq1"),
                            t("outercalcabs"),
                            t("outercalcrel"),
                            t("inner",
                                t("innerq1"),
                                t("innercalcabs"),
                                t("innercalcrel")
                            )
                        )
                    )),
                    bind("/data/outer/outercalcabs").calculate("/data/outer/outerq1 + 1"),
                    bind("/data/outer/outercalcrel").calculate("../outerq1 + 1"),
                    bind("/data/outer/inner/innercalcabs").calculate("/data/outer/inner/innerq1 + 2"),
                    bind("/data/outer/inner/innercalcrel").calculate("../innerq1 + 2")
                )),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerq1"),
                    repeat("/data/outer/inner",
                        input("/data/outer/inner/innerq1"))
                )
            )));

        scenario.answer("/data/outer[0]/outerq1", "5");
        assertThat(scenario.answerOf("/data/outer[0]/outercalcabs"), is(new IntegerData(6)));
        assertThat(scenario.answerOf("/data/outer[0]/outercalcrel"), is(new IntegerData(6)));

        scenario.createNewRepeat("/data/outer");

        scenario.answer("/data/outer[1]/outerq1", "23");
        // In a standards-compliant XPath engine, this would be 6 because /data/outer/outerq1 in the calculate expression
        // would always be equivalent to /data/outer[0]/outerq1
        assertThat(scenario.answerOf("/data/outer[1]/outercalcabs"), is(new IntegerData(24)));
        assertThat(scenario.answerOf("/data/outer[1]/outercalcrel"), is(new IntegerData(24)));

        scenario.answer("/data/outer[0]/inner[0]/innerq1", 18);
        assertThat(scenario.answerOf("/data/outer[0]/inner[0]/innercalcabs"), is(new IntegerData(20)));
        assertThat(scenario.answerOf("/data/outer[0]/inner[0]/innercalcrel"), is(new IntegerData(20)));

        scenario.createNewRepeat("/data/outer[0]/inner");

        scenario.answer("/data/outer[0]/inner[1]/innerq1", 19);
        // In a standards-compliant XPath engine, this would be 20 because /data/outer/inner/innerq1 in the calculate expression
        // would always be equivalent to /data/outer[0]/inner[0]/innerq1
        assertThat(scenario.answerOf("/data/outer[0]/inner[1]/innercalcabs"), is(new IntegerData(21)));
        assertThat(scenario.answerOf("/data/outer[0]/inner[1]/innercalcrel"), is(new IntegerData(21)));
    }
}
