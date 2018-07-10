/*
 * Copyright 2018 Nafundi
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
package org.javarosa.form.api;

import static org.javarosa.TestHelper.getFormEntryPrompt;
import static org.javarosa.TestHelper.getFormIndex;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.test.FormParseInit;
import org.junit.Before;
import org.junit.Test;

public class OutputInComputedConstraintTextTest {
    static {
        PrototypeManager.registerPrototype("org.javarosa.model.xform.XPathReference");
    }

    private FormDef formDef;
    private FormEntryController ctrl;

    @Before
    public void setUp() {
        FormParseInit fpi = new FormParseInit(r("constraint-message-error.xml"));
        formDef = fpi.getFormDef();
        formDef.getLocalizer().setLocale("English");
        ctrl = fpi.getFormEntryController();
    }

    @Test
    public void testComputedQuestionText() {
        // Answer first question to check label and constraint texts on next questions
        ctrl.answerQuestion(getFormIndex(formDef, "/constraintMessageError/village"), new LongData(1), true);

        assertEquals(
            "Please only conduct this survey with children aged 6 TO 24 MONTHS.",
            getFormEntryPrompt(formDef, "/constraintMessageError/ageSetNote").getQuestionText()
        );
    }

    @Test
    public void testComputedConstraintText() {
        // Answer first question to check label and constraint texts on next questions
        ctrl.answerQuestion(getFormIndex(formDef, "/constraintMessageError/village"), new LongData(1), true);

        assertEquals(
            "Please only conduct this survey with children aged 6 TO 24 MONTHS.",
            getFormEntryPrompt(formDef, "/constraintMessageError/age").getConstraintText()
        );
    }
}