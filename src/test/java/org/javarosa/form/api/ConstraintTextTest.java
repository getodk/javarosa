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

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.xform.parse.ParseException;
import org.junit.Test;

import java.util.Arrays;

public class ConstraintTextTest {

    @Test
    public void checkConstraintTexts() throws ParseException {
        FormParseInit fpi = new FormParseInit(r("constraint-message-error.xml"));
        FormEntryModel formEntryModel = fpi.getFormEntryModel();
        FormDef formDef = fpi.getFormDef();
        formDef.getLocalizer().setLocale("English");

        for (String expectedText : Arrays.asList("Your message", "Message", "Your message", "Message")) {
            formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));
            FormEntryPrompt formEntryPrompt = new FormEntryPrompt(formDef, formEntryModel.getFormIndex());
            assertEquals(expectedText, formEntryPrompt.getConstraintText());
        }
    }
}