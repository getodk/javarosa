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

import static org.javarosa.form.api.FormEntryController.ANSWER_CONSTRAINT_VIOLATED;
import static org.javarosa.form.api.FormEntryController.ANSWER_OK;
import static org.javarosa.form.api.FormEntryController.EVENT_END_OF_FORM;
import static org.junit.Assert.fail;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.form.api.FormEntryController;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * See testAnswerConstraint() for an example of how to write the
 * constraint unit type tests.
 */
public class FormDefTest {
    private FormParseInit fpi;

    @BeforeClass
    public static void init() {
        PrototypeManager.registerPrototype("org.javarosa.model.xform.XPathReference");
        ExtUtil.defaultPrototypes();
    }

    @Before
    public void setUp() {
        fpi = new FormParseInit();
    }

    @Test
    public void testAnswerConstraint() {
        IntegerData ans = new IntegerData(13);
        FormEntryController fec = fpi.getFormEntryController();
        fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        do {
            QuestionDef q = fpi.getCurrentQuestion();
            if (q == null || q.getTextID() == null || q.getTextID().length() == 0)
                continue;
            if (q.getTextID().equals("constraint-test")) {
                int response = fec.answerQuestion(ans, true);
                if (response == ANSWER_CONSTRAINT_VIOLATED)
                    fail("Answer Constraint test failed.");
                else if (response == ANSWER_OK)
                    break;
                else
                    fail("Bad response from fec.answerQuestion()");
            }
        } while (fec.stepToNextEvent() != EVENT_END_OF_FORM);
    }
}
