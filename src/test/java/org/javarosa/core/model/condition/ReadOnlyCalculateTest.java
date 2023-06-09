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

package org.javarosa.core.model.condition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.ParseException;
import org.junit.Test;

public class ReadOnlyCalculateTest {
    /**
     * Read-only is only a UI concern so calculates should be evaluated on read-only fields.
     */
    @Test
    public void calculate_evaluatedOnReadonlyFieldWithUi() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Calculate readonly", html(
            head(
                title("Calculate readonly"),
                model(
                    mainInstance(t("data id=\"calculate-readonly\"",
                        t("readonly-calculate")
                    )),
                    bind("/data/readonly-calculate").readonly("1").calculate("7 * 2")
                )
            ),
            body(
                input("/data/readonly-calculate")
            )));

        assertThat(scenario.answerOf("/data/readonly-calculate"), is(intAnswer(14)));
    }
}
