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

package org.javarosa.core.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.instance;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.select1Dynamic;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

import java.io.IOException;

import org.javarosa.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class FormDefSerializationTest {
    @Test public void instanceName_forReferenceInMainInstance_isAlwaysNull() throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = getSimplestFormScenario();

        scenario.next();
        assertThat(scenario.refAtIndex().getInstanceName(), is(nullValue()));

        Scenario deserialized = scenario.serializeAndDeserializeForm();

        deserialized.next();
        assertThat(deserialized.refAtIndex().getInstanceName(), is(nullValue()));
    }

    // During form evaluation, most relative references are contextualized directly or indirectly using the FormDef evaluation context.
    @Test public void instanceName_forFormDefEvaluationContext_isAlwaysNull() throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = getSimplestFormScenario();

        scenario.next();
        assertThat(scenario.getFormDef().getEvaluationContext().getContextRef().getInstanceName(), is(nullValue()));

        Scenario deserialized = scenario.serializeAndDeserializeForm();

        deserialized.next();
        assertThat(deserialized.getFormDef().getEvaluationContext().getContextRef().getInstanceName(), is(nullValue()));
    }

    // Constraint evaluation uses XPathPathExprEval.eval which uses the main instance from the FormDef to get a
    // TreeReference. Then XPathPathExpr.getRefValue sees whether that reference is the same as the latest modified
    // question by using TreeRefence.equals. In the original JavaRosa implementation, the main instance name was null prior
    // to serialization and set after deserialization.
    @Test public void instanceName_forFormDefMainInstance_isAlwaysNull() throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = getSimplestFormScenario();

        scenario.next();
        assertThat(scenario.getFormDef().getMainInstance().getBase().getInstanceName(), is(nullValue()));

        Scenario deserialized = scenario.serializeAndDeserializeForm();

        deserialized.next();
        assertThat(deserialized.getFormDef().getMainInstance().getBase().getInstanceName(), is(nullValue()));
    }

    @Test public void serializeAndDeserializeFormWithDefaultAnswerInSelectQuestion_worksCorrectly() throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Internal choices", html(
            head(
                title("Internal choices"),
                model(
                    mainInstance(
                        t("data id='main-instance'",
                            t("select-from-secondary-instance", "a")
                        )
                    ),
                    instance("secondary-instance",
                        t("item",
                            t("label", "A"),
                            t("value", "a")
                        )
                    )
                )
            ),
            body(
                select1Dynamic("/data/select-from-secondary-instance", "instance('secondary-instance')/root/item")
            ))
        );

        scenario.serializeAndDeserializeForm();
    }

    private static Scenario getSimplestFormScenario() throws IOException, XFormParser.ParseException {
        return Scenario.init("Simplest", html(
            head(
                title("Simplest"),
                model(
                    mainInstance(t("data id=\"simplest\"",
                        t("a")
                    )),
                    bind("/data/a").type("string")
                )
            ),
            body(
                input("/data/a")
            )));
    }
}
