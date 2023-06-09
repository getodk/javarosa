/*
 * Copyright (C) 2021 ODK
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

package org.javarosa.form.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.instance;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.select1Dynamic;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.ParseException;
import org.junit.Test;

public class FormEntryPromptTest {
    //region Binding of select choice values to labels
    @Test
    public void getSelectItemText_onSelectionFromDynamicSelect_withoutTranslations_returnsLabelInnerText() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Select", html(
            head(
                title("Select"),
                model(
                    mainInstance(
                        t("data id='select'",
                            t("filter"),
                            t("select", "a"))),

                    instance("choices",
                        item("a", "A"),
                        item("aa", "AA"),
                        item("b", "B"),
                        item("bb", "BB")))),
            body(
                input("/data/filter"),
                select1Dynamic("/data/select", "instance('choices')/root/item[starts-with(value,/data/filter)]")
            )));

        scenario.next();
        scenario.answer("a");

        scenario.next();
        FormEntryPrompt questionPrompt = scenario.getFormEntryPromptAtIndex();
        assertThat(questionPrompt.getAnswerText(), is("A"));
    }

    @Test
    public void getSelectItemText_onSelectionFromDynamicSelect_withTranslations_returnsCorrectTranslation() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Multilingual dynamic select", html(
            head(
                title("Multilingual dynamic select"),
                model(
                    t("itext",
                        t("translation lang='fr'",
                            t("text id='choices-0'",
                                t("value", "A (fr)")),
                            t("text id='choices-1'",
                                t("value", "B (fr)")),
                            t("text id='choices-2'",
                                t("value", "C (fr)"))
                        ),
                        t("translation lang='en'",
                            t("text id='choices-0'",
                                t("value", "A (en)")),
                            t("text id='choices-1'",
                                t("value", "B (en)")),
                            t("text id='choices-2'",
                                t("value", "C (en)"))
                        )),
                    mainInstance(
                        t("data id='multilingual-select'",
                            t("select", "b"))),

                    instance("choices",
                        t("item", t("itextId", "choices-0"), t("name", "a")),
                        t("item", t("itextId", "choices-1"), t("name", "b")),
                        t("item", t("itextId", "choices-2"), t("name", "c"))))),
            body(
                select1Dynamic("/data/select", "instance('choices')/root/item", "name", "jr:itext(itextId)"))
        ));

        scenario.setLanguage("en");

        scenario.next();
        FormEntryPrompt questionPrompt = scenario.getFormEntryPromptAtIndex();
        assertThat(questionPrompt.getAnswerText(), is("B (en)"));

        scenario.setLanguage("fr");
        assertThat(questionPrompt.getAnswerText(), is("B (fr)"));
    }

    @Test
    public void getSelectItemText_onSelectionsInRepeatInstances_returnsLabelInnerText() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Select", html(
            head(
                title("Select"),
                model(
                    mainInstance(
                        t("data id='select-repeat'",
                            t("repeat",
                                t("select", "a")),
                            t("repeat",
                                t("select", "a")))),

                    instance("choices",
                        item("a", "A"),
                        item("aa", "AA"),
                        item("b", "B"),
                        item("bb", "BB")))),
            body(
                repeat("/data/repeat",
                    select1Dynamic("/data/repeat/select", "instance('choices')/root/item")
            ))));

        scenario.next();
        scenario.next();
        FormEntryPrompt questionPrompt = scenario.getFormEntryPromptAtIndex();
        assertThat(questionPrompt.getAnswerText(), is("A"));

        // Prior to https://github.com/getodk/javarosa/issues/642 being addressed, the selected choice for a select in a
        // repeat instance with the same choice list as the prior repeat instance's select would not be bound to its label
        scenario.next();
        scenario.next();
        questionPrompt = scenario.getFormEntryPromptAtIndex();
        assertThat(questionPrompt.getAnswerText(), is("A"));
    }
    //endregion
}
