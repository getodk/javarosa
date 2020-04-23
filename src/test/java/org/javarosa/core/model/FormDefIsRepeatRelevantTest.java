package org.javarosa.core.model;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.test.Scenario;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.Scenario.getRef;
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

public class FormDefIsRepeatRelevantTest {
    @Test
    public void repeatRelevanceChanges_whenDependentValuesOfRelevanceExpressionChange() throws IOException {
        Scenario scenario = Scenario.init("Repeat relevance - dynamic expression", html(
            head(
                title("Repeat relevance - dynamic expression"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_dynamic\"",
                        t("selectYesNo", "no"),
                        t("repeat1",
                            t("q1"))
                        )),
                    bind("/data/repeat1").relevant("/data/selectYesNo = 'yes'")
                ),
                body(
                    select1("/data/selectYesNo",
                        item("yes", "Yes"),
                        item("no", "No")),
                    repeat("/data/repeat1",
                        input("/data/repeat1/q1")
                    )
                ))));
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/repeat1[0]")), is(false));

        scenario.answer("/data/selectYesNo", "yes");
        assertThat(formDef.isRepeatRelevant(getRef("/data/repeat1[0]")), is(true));
    }

    @Test
    public void repeatIsIrrelevant_whenRelevanceSetToFalse() throws IOException {
        Scenario scenario = Scenario.init("Repeat relevance - false()", html(
            head(
                title("Repeat relevance - false()"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_false\"",
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
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/repeat1[0]")), is(false));
    }

    @Test
    public void emptyRepeat_isIrrelevant() throws IOException {
        Scenario scenario = Scenario.init("Repeat relevance - empty repeat", html(
            head(
                title("Repeat relevance - empty repeat"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_empty\"",
                        t("emptyRepeat")
                    ))
                ),
                body(
                    repeat("/data/emptyRepeat")
                ))));
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/emptyRepeat[0]")), is(false));
    }

    @Test
    public void repeatRelevanceChanges_whenDependentValuesOfGrandparentRelevanceExpressionChange() throws IOException {
        Scenario scenario = Scenario.init("Repeat relevance - dynamic expression", html(
            head(
                title("Repeat relevance - dynamic expression"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_dynamic\"",
                        t("selectYesNo", "no"),
                        t("outer",
                            t("inner",
                                t("repeat1",
                                    t("q1"))
                            )
                        )
                    )),
                    bind("/data/outer").relevant("/data/selectYesNo = 'yes'")
                ),
                body(
                    select1("/data/selectYesNo",
                        item("yes", "Yes"),
                        item("no", "No")),
                    repeat("/data/outer/inner/repeat1",
                        input("/data/outer/inner/repeat1/q1")
                    )
                ))));
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/outer/inner/repeat1[0]")), is(false));

        scenario.answer("/data/selectYesNo", "yes");
        assertThat(formDef.isRepeatRelevant(getRef("/data/outer/inner/repeat1[0]")), is(true));
    }

    @Test
    public void repeatIsIrrelevant_whenGrandparentRelevanceSetToFalse() throws IOException {
        Scenario scenario = Scenario.init("Repeat relevance - false()", html(
            head(
                title("Repeat relevance - false()"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_false\"",
                        t("outer",
                            t("inner",
                                t("repeat1",
                                    t("q1")
                                )
                            )
                        )
                    )),
                    bind("/data/outer").relevant("false()")
                ),
                body(
                    repeat("/data/outer/inner/repeat1",
                        input("/data/outer/inner/repeat1/q1")
                    )
                ))));
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/outer/inner/repeat1[0]")), is(false));
    }
}