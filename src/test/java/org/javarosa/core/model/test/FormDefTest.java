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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.test.Scenario.AnswerResult.CONSTRAINT_VIOLATED;
import static org.javarosa.test.Scenario.AnswerResult.OK;
import static org.javarosa.test.Scenario.getRef;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.item;
import static org.javarosa.test.XFormsElement.label;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.meta;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.repeat;
import static org.javarosa.test.XFormsElement.select1;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.javarosa.test.ResourcePathHelper.r;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.FormInitializationMode;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.test.Scenario;
import org.javarosa.test.XFormsElement;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;
/**
 * See testAnswerConstraint() for an example of how to write the
 * constraint unit type tests.
 */
public class FormDefTest {
    @Test
    public void enforces_constraints_defined_in_a_field() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("ImageSelectTester.xhtml"));
        scenario.next();
        scenario.next();
        scenario.next();
        scenario.next();
        scenario.next();
        assertThat(scenario.answer("10"), Matchers.is(CONSTRAINT_VIOLATED));
        assertThat(scenario.answer("13"), Matchers.is(OK));
    }

    @Test
    public void enforcesConstraints_whenInstanceIsDeserialized() throws IOException, XFormParser.ParseException {
        XFormsElement formDef = html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("a")
                    )),
                    bind("/data/a").type("string").constraint("regex(.,'[0-9]{10}')")
                )
            ),
            body(input("/data/a"))
        );

        Scenario scenario = Scenario.init("Some form", formDef);

        scenario.next();
        Scenario.AnswerResult result = scenario.answer("00000");
        assertThat(result, Matchers.is(CONSTRAINT_VIOLATED));

        scenario.answer("0000000000");
        scenario.next();
        assertThat(scenario.getCurrentIndex().isEndOfFormIndex(), is(true));

        Scenario restored = scenario.serializeAndDeserializeInstance(formDef);
        restored.next();
        assertThat(restored.answerOf("/data/a"), is(stringAnswer("0000000000")));
        result = restored.answer("00000");
        assertThat(result, Matchers.is(CONSTRAINT_VIOLATED));
    }

    //region Repeat relevance
    @Test
    public void repeatRelevanceChanges_whenDependentValuesOfRelevanceExpressionChange() throws IOException, XFormParser.ParseException {
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
                )),
            body(
                select1("/data/selectYesNo",
                    item("yes", "Yes"),
                    item("no", "No")),
                repeat("/data/repeat1",
                    input("/data/repeat1/q1")
                )
            )));
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/repeat1[1]")), is(false));

        scenario.answer("/data/selectYesNo", "yes");
        assertThat(formDef.isRepeatRelevant(getRef("/data/repeat1[1]")), is(true));
    }

    @Test
    public void repeatIsIrrelevant_whenRelevanceSetToFalse() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Repeat relevance - false()", html(
            head(
                title("Repeat relevance - false()"),
                model(
                    mainInstance(t("data id=\"repeat_relevance_false\"",
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
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/repeat1[0]")), is(false));
    }

    @Test
    public void repeatRelevanceChanges_whenDependentValuesOfGrandparentRelevanceExpressionChange() throws IOException, XFormParser.ParseException {
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
                )),
            body(
                select1("/data/selectYesNo",
                    item("yes", "Yes"),
                    item("no", "No")),
                repeat("/data/outer/inner/repeat1",
                    input("/data/outer/inner/repeat1/q1")
                )
            )));
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/outer/inner/repeat1[0]")), is(false));

        scenario.answer("/data/selectYesNo", "yes");
        assertThat(formDef.isRepeatRelevant(getRef("/data/outer/inner/repeat1[0]")), is(true));
    }

    @Test
    public void repeatIsIrrelevant_whenGrandparentRelevanceSetToFalse() throws IOException, XFormParser.ParseException {
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
                )),
            body(
                repeat("/data/outer/inner/repeat1",
                    input("/data/outer/inner/repeat1/q1")
                )
            )));
        FormDef formDef = scenario.getFormDef();

        assertThat(formDef.isRepeatRelevant(getRef("/data/outer/inner/repeat1[0]")), is(false));
    }

    @Test
    public void nestedRepeatRelevance_updatesBasedOnParentPosition() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Nested repeat relevance", html(
            head(
                title("Nested repeat relevance"),
                model(
                    mainInstance(t("data id=\"nested-repeat-relevance\"",
                        t("outer",
                            t("inner",
                                t("q1")
                            ),
                            t("inner",
                                t("q1")
                            )
                        ),
                        t("outer",
                            t("inner",
                                t("q1")
                            )
                        ),
                        t("relevance-condition", "0")
                    )),
                    bind("/data/relevance-condition").type("string"),
                    bind("/data/outer/inner").relevant("position(..) mod 2 = /data/relevance-condition")
                )),
            body(
                repeat("/data/outer",
                    repeat("/data/outer/inner",
                        input("/data/outer/inner/q1")
                    )
                ),
                input("/data/relevance-condition")
            )));


        scenario.next();

        // For ref /data/outer[1]/inner[1], the parent position is 1 so the boolean expression is false. That means
        // none of the inner groups in /data/outer[1] can be relevant.
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[1]")));

        scenario.next();
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[2]")));

        scenario.next();
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[2]/inner[1]")));

        scenario.next();
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[2]/inner[1]/q1[1]")));

        scenario.answer("/data/relevance-condition", "1");

        scenario.jumpToBeginningOfForm();
        scenario.next();
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[1]")));

        scenario.next();
        assertThat(scenario.refAtIndex(), is(getRef("/data/outer[1]/inner[1]")));
    }

    @Test
    public void innerRepeatGroupIsIrrelevant_whenItsParentRepeatGroupDoesNotExist() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Nested repeat relevance", html(
            head(
                title("Nested repeat relevance"),
                model(
                    mainInstance(t("data id=\"nested-repeat-relevance\"",
                        t("outer",
                            t("inner",
                                t("q1")
                            )
                        )
                    ))
                )),
            body(
                repeat("/data/outer",
                    repeat("/data/outer/inner",
                        input("/data/outer/inner/q1")
                    )
                )
            )));


        FormDef formDef = scenario.getFormDef();
        // outer[2] does not exist at this moment, we only have outer[1]. Checking if its inner repeat group is relevant should be possible and return false.
        assertThat(formDef.isRepeatRelevant(getRef("/data/outer[2]/inner[1]")), is(false));
    }
    //endregion

    @Test
    public void canCreateRepeat_returnsFalse_when_repeatCountSetButTheGroupItBelongsToDoesNotExist() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Nested repeat relevance", html(
            head(
                title("Nested repeat relevance"),
                model(
                    mainInstance(t("data id=\"nested-repeat-relevance\"",
                        t("outer",
                            t("inner_count"),
                            t("inner",
                                t("question")
                            )
                        )
                    )),
                    bind("/data/outer/inner_count").type("string").calculate("5")
                )),
            body(
                repeat("/data/outer",
                    repeat("/data/outer/inner", "/data/outer/inner_count",
                        input("/data/outer/inner/question")
                    )
                )
            )));

        scenario.next();
        FormIndex outerGroupIndex = scenario.getCurrentIndex();

        scenario.next();
        TreeReference innerGroupRef = scenario.refAtIndex();
        FormIndex index = scenario.getCurrentIndex();

        FormDef formDef = scenario.getFormDef();
        formDef.deleteRepeat(outerGroupIndex);

        assertThat(formDef.canCreateRepeat(innerGroupRef, index), is(false));
    }

    @Test
    public void fillTemplateString_resolvesRelativeReferences() throws IOException, XFormParser.ParseException {
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
        assertThat(caption.getQuestionText(), is("Position: 1"));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();

        caption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(caption.getQuestionText(), is("Position: 2"));
    }

    @Test
    public void fillTemplateString_resolvesRelativeReferences_inItext() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("<output> with relative ref in translation", html(
            head(
                title("output with relative ref in translation"),
                model(
                    t("itext", t("translation lang=\"Français\"",
                        t("text id=\"/data/repeat/position_in_label:label\"",
                            t("value", "Position: <output value=\"../position\"/>"))
                    )),
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
        assertThat(caption.getQuestionText(), is("Position: 1"));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();

        caption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(caption.getQuestionText(), is("Position: 2"));
    }

    @Test
    public void canAddFunctionHandlersBeforeInitialize() throws Exception {
        FormDef formDef = Scenario.createFormDef("custom-func-form", html(
            head(
                title("custom-func-form"),
                model(
                    mainInstance(t("data",
                        t("calculate"),
                        t("input")
                    )),
                    bind("/data/calculate").type("string").calculate("custom-func()")
                )
            ),
            body(
                input("/data/input",
                    label("/data/calculate")
                )
            )
        ));

        formDef.getEvaluationContext().addFunctionHandler(new IFunctionHandler() {
            @Override
            public String getName() {
                return "custom-func";
            }

            @Override
            public List<Class[]> getPrototypes() {
                return new ArrayList<Class[]>();
            }

            @Override
            public boolean rawArgs() {
                return true;
            }

            @Override
            public boolean realTime() {
                return false;
            }

            @Override
            public Object eval(Object[] args, EvaluationContext ec) {
                return "blah";
            }
        });

        Scenario.init(formDef);
    }

    @Test public void addDeprecatedIDAndUpdateInstanceID_whenFormDefInitializedForFinalizedFormEdit() throws IOException, XFormParser.ParseException {
        FormDef formDef = Scenario.createFormDef("Simplest", html(
            head(
                title("Simplest"),
                model(
                    mainInstance(t("data id=\"simplest\"",
                        t("a"),
                        meta(t("instanceID"))
                    )),
                    bind("/data/a").type("string"),
                    bind("/data/meta/instanceID").preload("uid")
                )
            ),
            body(
                input("/data/a")
            )));

        formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("instanceID").setAnswer(new StringData("originalInstanceId"));
        assertThat(
            formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("deprecatedID"),
            is(nullValue())
        );

        formDef.initialize(FormInitializationMode.FINALIZED_FORM_EDIT);

        IAnswerData newInstanceID = formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("instanceID").getValue();
        IAnswerData deprecatedID = formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("deprecatedID").getValue();

        assertThat(newInstanceID.getValue(), not("originalInstanceId"));
        assertThat(deprecatedID.getValue(), is("originalInstanceId"));
    }

    @Test public void updateInstanceIDAndDeprecatedID_whenFormDefInitializedForSubsequentEditsOfFinalizedForm() throws IOException, XFormParser.ParseException {
        FormDef formDef = Scenario.createFormDef("Simplest", html(
            head(
                title("Simplest"),
                model(
                    mainInstance(t("data id=\"simplest\"",
                        t("a"),
                        meta(t("instanceID"))
                    )),
                    bind("/data/a").type("string"),
                    bind("/data/meta/instanceID").preload("uid")
                )
            ),
            body(
                input("/data/a")
            )));

        formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("instanceID").setAnswer(new StringData("originalInstanceId"));
        formDef.initialize(FormInitializationMode.FINALIZED_FORM_EDIT);

        IAnswerData originalInstanceID = formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("instanceID").getValue();
        IAnswerData originalDeprecatedID = formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("deprecatedID").getValue();

        formDef.initialize(FormInitializationMode.FINALIZED_FORM_EDIT);

        IAnswerData newInstanceID = formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("instanceID").getValue();
        IAnswerData newDeprecatedID = formDef.getMainInstance().getRoot().getFirstChild("meta").getFirstChild("deprecatedID").getValue();

        assertThat(newDeprecatedID.getValue(), not(originalDeprecatedID.getValue()));
        assertThat(newDeprecatedID.getValue(), is(originalInstanceID.getValue()));
        assertThat(originalInstanceID.getValue(), not(newInstanceID.getValue()));
    }
}
