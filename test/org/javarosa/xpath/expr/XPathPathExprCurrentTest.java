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

public class XPathPathExprCurrentTest {
    private FormDef formDef;
    private FormEntryController formEntryController;

    @Before
    public void setUp() {
        FormParseInit fpi = new FormParseInit(r("relative-current-ref.xml"));
        formDef = fpi.getFormDef();
        formDef.initialize(true, new InstanceInitializationFactory());
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        formEntryController = new FormEntryController(formEntryModel);
    }

    /**
     * current() in a calculate should refer to the node it is in (in this case, /data/my_group/name_relative).
     * This means that to refer to a sibling node, the path should be current()/../<name of sibling node>. This is
     * verified by changing the value of the node that the calculate is supposed to refer to
     * (/data/my_group/name) and seeing that the dependent calculate is updated accordingly.
     */
    @Test
    public void current_in_calculate_should_refer_to_node() {
        formEntryController.jumpToFirstQuestionWithName("name");
        StringData nameValue = new StringData("Bob");
        formEntryController.answerQuestion(nameValue, true);

        StringData relativeName = (StringData) formDef.getFirstDescendantWithName("name").getValue();
        assertEquals(nameValue.getValue(), relativeName.getValue());
    }

    /**
     * current() in a choice filter should refer to the select node the choice filter is called from, NOT the expression
     * it is in. See https://developer.mozilla.org/en-US/docs/Web/XPath/Functions/current -- this is the difference
     * between current() and .
     *
     * The behavior of current() in a choice filter is verified by selecting a value for a first, static select and then
     * using that value to filter a second, dynamic select.
     */
    @Test
    public void current_in_choice_filter_should_refer_to_node() {
        formEntryController.jumpToFirstQuestionWithName("fruit");
        Selection fruitSelection = new Selection(1);
        fruitSelection.attachChoice(formEntryController.getModel().getQuestionPrompt().getQuestion());
        formEntryController.answerQuestion(new SelectOneData(fruitSelection), true);

        SelectOneData fruitSelectionValue = (SelectOneData) formDef.getFirstDescendantWithName("fruit").getValue();
        assertEquals("blueberry", fruitSelectionValue.getDisplayText());

        // === Variety question ===
        formEntryController.stepToNextEvent();
        // Collect calls getAnswerValue to populate dynamic choices
        FormEntryPrompt varietyPrompt = formEntryController.getModel().getQuestionPrompt();
        varietyPrompt.getAnswerValue();
        ItemsetBinding dynamicChoices = varietyPrompt.getQuestion().getDynamicChoices();

        // There are three blueberry varieties defined by the form
        assertEquals(dynamicChoices.getChoices().size(), 3);

        SelectChoice varietyChoice = dynamicChoices.getChoices().get(1);
        SelectOneData varietySelection = new SelectOneData(new Selection(varietyChoice));
        assertEquals("Collins", varietyPrompt.getSelectChoiceText(varietyChoice));
        formEntryController.answerQuestion(varietySelection, true);

        SelectOneData variety = (SelectOneData) formDef.getFirstDescendantWithName("variety").getValue();
        assertEquals("collins", variety.getDisplayText());
    }
}