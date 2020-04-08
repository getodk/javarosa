package org.javarosa.form.api;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.group;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;

public class FormEntryControllerTest {

    @Test
    public void jumpToNewRepeatPrompt_whenInRepeat_jumpsToRepeatPrompt() throws Exception {
        Scenario scenario = Scenario.init("repeat", html(
            head(
                title("form"),
                model(
                    mainInstance(
                        t("data",
                            t("repeat",
                                t("question1"),
                                t("question2")
                            )
                        )
                    ),
                    bind("/data/repeat/question1").type("int"),
                    bind("/data/repeat/question2").type("int")
                )
            ),
            body(
                group("/data/repeat",
                    repeat("/data/repeat",
                        input("/data/repeat/question1"),
                        input("/data/repeat/question2")
                    )
                )
            )
        ));

        FormEntryController controller = new FormEntryController(new FormEntryModel(scenario.getFormDef()));

        assertThat(controller.stepToNextEvent(), is(EVENT_REPEAT));
        assertThat(controller.stepToNextEvent(), is(EVENT_QUESTION));
        assertThat(controller.getModel().getFormIndex().toString(), is("0_0, 0, "));

        controller.jumpToNewRepeatPrompt();
        assertThat(controller.getModel().getFormIndex().toString(), is("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenInOuterOfNestedRepeat_jumpsToOuterRepeatPrompt() throws Exception {
        Scenario scenario = Scenario.init("nestedRepeat", html(
            head(
                title("form"),
                model(
                    mainInstance(
                        t("data",
                            t("repeat1",
                                t("question1"),
                                t("question2"),
                                t("repeat2",
                                    t("question3")
                                )
                            )
                        )
                    ),
                    bind("/data/repeat1/question1").type("int"),
                    bind("/data/repeat1/question2").type("int"),
                    bind("/data/repeat1/repeat2/question3").type("int")
                )
            ),
            body(
                group("/data/repeat1",
                    repeat("/data/repeat1",
                        input("/data/repeat1/question1"),
                        input("/data/repeat1/question2"),
                        group("/data/repeat1/repeat2",
                            repeat("/data/repeat1/repeat2",
                                input("/data/repeat1/repeat2/question3")
                            )
                        )
                    )
                )
            )
        ));

        FormEntryController controller = new FormEntryController(new FormEntryModel(scenario.getFormDef()));

        assertThat(controller.stepToNextEvent(), is(EVENT_REPEAT));
        assertThat(controller.stepToNextEvent(), is(EVENT_QUESTION));
        assertThat(controller.getModel().getFormIndex().toString(), is("0_0, 0, "));

        controller.jumpToNewRepeatPrompt();
        assertThat(controller.getModel().getFormIndex().toString(), is("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenInInnerOfNestedRepeat_jumpsToInnerRepeatPrompt() throws Exception {
        Scenario scenario = Scenario.init("nestedRepeat", html(
            head(
                title("form"),
                model(
                    mainInstance(
                        t("data",
                            t("repeat1",
                                t("question1"),
                                t("question2"),
                                t("repeat2",
                                    t("question3")
                                )
                            )
                        )
                    ),
                    bind("/data/repeat1/question1").type("int"),
                    bind("/data/repeat1/question2").type("int"),
                    bind("/data/repeat1/repeat2/question3").type("int")
                )
            ),
            body(
                group("/data/repeat1",
                    repeat("/data/repeat1",
                        input("/data/repeat1/question1"),
                        input("/data/repeat1/question2"),
                        group("/data/repeat1/repeat2",
                            repeat("/data/repeat1/repeat2",
                                input("/data/repeat1/repeat2/question3")
                            )
                        )
                    )
                )
            )
        ));

        FormEntryController controller = new FormEntryController(new FormEntryModel(scenario.getFormDef()));

        assertThat(controller.stepToNextEvent(), is(EVENT_REPEAT));
        assertThat(controller.stepToNextEvent(), is(EVENT_QUESTION));
        assertThat(controller.stepToNextEvent(), is(EVENT_QUESTION));
        assertThat(controller.stepToNextEvent(), is(EVENT_REPEAT));
        assertThat(controller.stepToNextEvent(), is(EVENT_QUESTION));
        assertThat(controller.getModel().getFormIndex().toString(), is("0_0, 2_0, 0, "));

        controller.jumpToNewRepeatPrompt();
        assertThat(controller.getModel().getFormIndex().toString(), is("0_0, 2_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenInGroupInRepeat_jumpsToRepeatPrompt() throws Exception {
        Scenario scenario = Scenario.init("groupInRepeat", html(
            head(
                title("form"),
                model(
                    mainInstance(
                        t("data",
                            t("repeat",
                                t("group",
                                    t("question1"),
                                    t("question2")
                                )
                            )
                        )
                    ),
                    bind("/data/repeat/group/question1").type("int"),
                    bind("/data/repeat/group/question1").type("int")
                )
            ),
            body(
                group("/data/repeat",
                    repeat("/data/repeat",
                        group("/data/repeat/group",
                            input("/data/repeat/group/question1"),
                            input("/data/repeat/group/question2")
                        )
                    )
                )
            )
        ));

        FormEntryController controller = new FormEntryController(new FormEntryModel(scenario.getFormDef()));

        assertThat(controller.stepToNextEvent(), is(EVENT_REPEAT));
        assertThat(controller.stepToNextEvent(), is(EVENT_GROUP));
        assertThat(controller.stepToNextEvent(), is(EVENT_QUESTION));
        assertThat(controller.getModel().getFormIndex().toString(), is("0_0, 0, 0, "));

        controller.jumpToNewRepeatPrompt();
        assertThat(controller.getModel().getFormIndex().toString(), is("0_1, "));
    }

    @Test
    public void jumpToNewRepeatPrompt_whenNotInRepeat_doesNothing() throws Exception {
        Scenario scenario = Scenario.init("questionsOnly", html(
            head(
                title("form"),
                model(
                    mainInstance(
                        t("data",
                            t("question1"),
                            t("question2")
                        )
                    ),
                    bind("/data/question1").type("int"),
                    bind("/data/question1").type("int")
                )
            ),
            body(
                input("/data/question1"),
                input("/data/question2")
            )
        ));

        FormEntryController controller = new FormEntryController(new FormEntryModel(scenario.getFormDef()));
        assertThat(controller.stepToNextEvent(), is(EVENT_QUESTION));
        assertThat(controller.getModel().getFormIndex().toString(), is("0, "));

        controller.jumpToNewRepeatPrompt();
        assertThat(controller.getModel().getFormIndex().toString(), is("0, "));
    }
}