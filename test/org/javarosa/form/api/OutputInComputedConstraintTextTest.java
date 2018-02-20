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

import java.util.HashMap;
import java.util.Map;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.test.FormParseInit;
import org.junit.Before;
import org.junit.Test;

public class OutputInComputedConstraintTextTest {
  static {
    PrototypeManager.registerPrototype("org.javarosa.model.xform.XPathReference");
  }

  private Map<String, FormIndex> formIndexesById = new HashMap<>();
  private FormDef formDef;
  private FormEntryController ctrl;
  private FormEntryModel model;

  @Before
  public void setUp() {
    FormParseInit fpi = new FormParseInit();
    fpi.setFormToParse(r("constraint-message-error.xml").toString());
    formDef = fpi.getFormDef();
    formDef.getLocalizer().setLocale("English");
    ctrl = fpi.getFormEntryController();
    model = fpi.getFormEntryModel();
    buildIndexes();
  }

  @Test
  public void testComputedQuestionText() {
    // Answer first question to check label and constraint texts on next questions
    ctrl.answerQuestion(getFormIndex("/constraintMessageError/village:label"), new LongData(1), true);

    assertEquals(
        "Please only conduct this survey with children aged 6 TO 24 MONTHS.",
        getFormEntryPrompt("/constraintMessageError/ageSetNote:label").getQuestionText()
    );
  }

  @Test
  public void testComputedConstraintText() {
    // Answer first question to check label and constraint texts on next questions
    ctrl.answerQuestion(getFormIndex("/constraintMessageError/village:label"), new LongData(1), true);

    assertEquals(
        "Please only conduct this survey with children aged 6 TO 24 MONTHS.",
        getFormEntryPrompt("/constraintMessageError/age:label").getConstraintText()
    );
  }

  private void buildIndexes() {
    ctrl.jumpToIndex(FormIndex.createBeginningOfFormIndex());
    do {
      FormEntryCaption fep = model.getCaptionPrompt();
      IFormElement formElement = fep.getFormElement();
      if (formElement instanceof QuestionDef)
        formIndexesById.put(formElement.getTextID(), fep.getIndex());
    } while (ctrl.stepToNextEvent() != FormEntryController.EVENT_END_OF_FORM);
  }

  private FormIndex getFormIndex(String id) {
    if (formIndexesById.containsKey(id))
      return formIndexesById.get(id);
    throw new RuntimeException("FormIndex with id \"" + id + "\" not found");
  }

  private FormEntryPrompt getFormEntryPrompt(String id) {
    return new FormEntryPrompt(formDef, getFormIndex(id));
  }

}