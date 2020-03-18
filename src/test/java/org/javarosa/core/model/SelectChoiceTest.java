/*
 * Copyright 2020 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.label;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.select1;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.junit.Test;

public class SelectChoiceTest {
    @Test
    public void value_should_continue_being_an_empty_string_after_deserialization() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("SelectChoice.getValue() regression test form", html(
            head(
                title("SelectChoice.getValue() regression test form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("the-choice")
                    )),
                    bind("/data/the-choice").type("string").required()
                )
            ),
            body(select1("/data/the-choice",
                label("Select one choice"),
                item("", "Empty value")
            ))
        ));

        scenario.next();
        assertThat(scenario.getQuestionAtIndex().getChoice(0).getValue(), is(""));

        Scenario deserializedScenario = scenario.serializeAndDeserializeForm();
        deserializedScenario.newInstance();
        deserializedScenario.next();
        assertThat(deserializedScenario.getQuestionAtIndex().getChoice(0).getValue(), is(""));
    }
}
