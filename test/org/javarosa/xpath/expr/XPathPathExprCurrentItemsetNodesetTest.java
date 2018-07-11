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
package org.javarosa.xpath.expr;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.javarosa.test.utils.ResourcePathHelper.r;

public class XPathPathExprCurrentItemsetNodesetTest {
    private FormDef formDef;
    private FormEntryController formEntryController;

    @Before
    public void setUp() {
        FormParseInit fpi = new FormParseInit(r("relative-current-ref-itemset-nodeset.xml"));
        formDef = fpi.getFormDef();
        formDef.initialize(true, new InstanceInitializationFactory());
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        formEntryController = new FormEntryController(formEntryModel);
    }

    /**
     * current() in an itemset nodeset expression should refer to the select node. This is verified by building an
     * itemset from a repeat which is a sibling of the select.
     */
    @Test
    public void current_in_itemset_nodeset_should_refer_to_node() {
        // don't know how to jump to repeat directly so jump to following question and step backwards
        formEntryController.jumpToFirstQuestionWithName("selected_person");
        formEntryController.stepToPreviousEvent(); // repeat
        formEntryController.descendIntoRepeat(0);
        formEntryController.stepToNextEvent(); // person

        StringData nameValue = new StringData("Bob");
        formEntryController.answerQuestion(nameValue, true);

        formEntryController.stepToNextEvent();
        formEntryController.descendIntoNewRepeat();
        formEntryController.stepToNextEvent(); // person

        StringData nameValue2 = new StringData("Janet");
        formEntryController.answerQuestion(nameValue2, true);

        formEntryController.jumpToFirstQuestionWithName("selected_person");
        FormEntryPrompt personPrompt = formEntryController.getModel().getQuestionPrompt();
        personPrompt.getAnswerValue();
        ItemsetBinding dynamicChoices = personPrompt.getQuestion().getDynamicChoices();

        SelectChoice personChoice = dynamicChoices.getChoices().get(1);
        assertEquals("Janet", personPrompt.getSelectChoiceText(personChoice));
        SelectOneData personSelection = new SelectOneData(new Selection(personChoice));
        formEntryController.answerQuestion(personSelection, true);

        SelectOneData personSelectionValue = (SelectOneData) formDef.getFirstDescendantWithName("selected_person")
                .getValue();
        assertEquals("2", personSelectionValue.getDisplayText());
    }
}
