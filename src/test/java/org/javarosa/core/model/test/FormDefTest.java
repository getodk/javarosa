/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.test;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.Scenario.AnswerResult.CONSTRAINT_VIOLATED;
import static org.javarosa.core.test.Scenario.AnswerResult.OK;
import static org.javarosa.core.test.Scenario.getRef;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.label;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.select1;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.test.Scenario;
import org.javarosa.form.api.FormEntryCaption;
import org.junit.Test;
/**
 * See testAnswerConstraint() for an example of how to write the
 * constraint unit type tests.
 */
public class FormDefTest {
    @Test
    public void enforces_constraints_defined_in_a_field() {
        Scenario scenario = Scenario.init(r("ImageSelectTester.xhtml"));
        scenario.next();
        scenario.next();
        scenario.next();
        scenario.next();
        scenario.next();
        assertThat(scenario.answer("10"), Matchers.is(CONSTRAINT_VIOLATED));
        assertThat(scenario.answer("13"), Matchers.is(OK));
    }

    //region Repeat relevance
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

    @Test
    public void nestedRepeatRelevance_updatesBasedOnParentPosition() throws IOException {
        Scenario scenario = Scenario.init("Nested repeat relevance", html(
            head(
                title("Nested repeat relevance"),
                model(
                    mainInstance(t("data id=\"nested-repeat-relevance\"",
                        t("outer",
                            t("inner",
                                t("q1")
                            )
                        ),
                        t("relevance-condition", "0")
                    )),
                    bind("/data/relevance-condition").type("string"),
                    bind("/data/outer/inner").relevant("position(..) mod 2 = /data/relevance-condition")
                ),
                body(
                    repeat("/data/outer",
                        repeat("/data/outer/inner",
                            input("/data/outer/inner/q1")
                        )
                    ),
                    input("/data/relevance-condition")
                ))));

        FormDef formDef = scenario.getFormDef();
        assertThat(formDef.isRepeatRelevant(getRef("/data/outer[0]/inner[0]")), is(false));

        scenario.next();
        scenario.next();
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[0]/inner[1]")));

        scenario.answer("/data/relevance-condition", "1");
        scenario.jumpToBeginningOfForm();

        scenario.next();
        scenario.next();
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[0]/inner[0]")));
    }
    //endregion

    @Test
    public void fillTemplateString_resolvesRelativeReferences() throws IOException {
        Scenario scenario = Scenario.init("<output> with relative ref", html(
            head(
                title("output with relative ref"),
                model(
                    mainInstance(t("data id=\"relative-output\"",
                        t("repeat jr:template=\"\"",
                            t("position"),
                            t("position_in_label")
                        )
                    )),
                    bind("/data/repeat/position").type("int").calculate("position(..)"),
                    bind("/data/repeat/position_in_label").type("int")
                )
            ),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/position_in_label", label("Position: <output value=\" ../position \"/>"))))
        ));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();

        FormEntryCaption caption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        MatcherAssert.assertThat(caption.getQuestionText(), is("Position: 1"));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();

        caption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        MatcherAssert.assertThat(caption.getQuestionText(), is("Position: 2"));
    }
}
