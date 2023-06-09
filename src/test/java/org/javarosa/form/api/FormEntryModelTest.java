/*
 * Copyright (C) 2020 Nafundi
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

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.group;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.ParseException;
import org.junit.Test;

public class FormEntryModelTest {
    @Test
    public void isIndexRelevant_respectsRelevanceOfOutermostGroup() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Nested relevance", html(
            head(
                title("Nested relevance"),
                model(
                    mainInstance(t("data id=\"nested_relevance\"",
                        t("outer",
                            t("inner",
                                t("q1"))),

                        t("innerYesNo", "no"),
                        t("outerYesNo", "no")
                        )),
                    bind("/data/outer").relevant("/data/outerYesNo = 'yes'"),
                    bind("/data/outer/inner").relevant("/data/innerYesNo = 'yes'")
                )),
            body(
                group("/data/outer",
                    group("/data/outer/inner",
                        input("/data/outer/inner/q1")
                    )
                ),
                input("/data/outerYesNo"),
                input("/data/innerYesNo")
            )));
        FormDef formDef = scenario.getFormDef();
        FormEntryModel formEntryModel = new FormEntryModel(formDef);

        FormIndex q1Index = scenario.indexOf("/data/outer/inner/q1");
        assertThat(formEntryModel.isIndexRelevant(q1Index), is(false));

        scenario.answer("/data/innerYesNo", "yes");
        assertThat(formEntryModel.isIndexRelevant(q1Index), is(false));

        scenario.answer("/data/outerYesNo", "yes");
        assertThat(formEntryModel.isIndexRelevant(q1Index), is(true));
    }
}
