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

package org.javarosa.formmanager.view.singlequestionscreen.screen;

import java.util.Enumeration;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.formmanager.view.FormElementBinding;

import de.enough.polish.ui.Style;

public class Select1QuestionScreen extends OneQuestionScreen {
	protected ChoiceGroup cg;

	public Select1QuestionScreen(FormElementBinding qDef,  Style style) {
		super(qDef,  style);
	}

	public void createView() {
		if (qDef.instanceNode.required) {
			// #style choiceGroup
			cg = new ChoiceGroup("*"
					+ ((QuestionDef) qDef.element).getLongText(),
					ChoiceGroup.EXCLUSIVE);
		} else {
			// #style choiceGroup
			cg = new ChoiceGroup(((QuestionDef) qDef.element).getLongText(),
					ChoiceGroup.EXCLUSIVE);
		}

		Enumeration itr = ((QuestionDef) qDef.element).getSelectItems().keys();// access
																				// choices
																				// directly

		int preselectionIndex = -1; // index of the preset value for the
									// question, if any
		String preselectionLabel = qDef.instanceNode.getValue() != null ? qDef.instanceNode
				.getValue().getDisplayText()
				: null;
		int count = 0;

		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();

			// check if the value is equal to the preset for this question
			if ((preselectionLabel != null)
					&& (label.equals(preselectionLabel)))
				preselectionIndex = count;

			cg.append(label, null);// add options to choice group

			count++;
		}
		this.append(cg);

		// set the selection to the preset value, if any
		if ((preselectionIndex > -1) && (preselectionIndex < cg.size()))
			cg.setSelectedIndex(preselectionIndex, true);

		this.addNavigationButtons();
		if (((QuestionDef) qDef.element).getHelpText() != null) {
			setHint(((QuestionDef) qDef.element).getHelpText());
		}

	}

	public IAnswerData getWidgetValue() {

		int selectedIndex = -1;

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {
				selectedIndex = i;
				break;
			}
		}

		QuestionDef question = (QuestionDef) qDef.element;
		Selection s = new Selection((String) question.getSelectItemIDs()
				.elementAt(selectedIndex));
		s.attachQuestionDef(question);

		return (selectedIndex == -1 ? null : new SelectOneData(s));
	}

}