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

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Style;

public class SelectOneQuestionScreen extends SingleQuestionScreen {
	protected ChoiceGroup cg;

	public SelectOneQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt,groupName,style);
	}

	public void createView() {
		if (prompt.isRequired()) {
			// #style choiceGroup
			cg = new ChoiceGroup("*"
					+ prompt.getLongText(),
					ChoiceGroup.EXCLUSIVE);
		} else {
			// #style choiceGroup
			cg = new ChoiceGroup(prompt.getLongText(),
					ChoiceGroup.EXCLUSIVE);
		}

		Enumeration itr = (prompt.getSelectChoices().elements());
		
		int preselectionIndex = -1; // index of the preset value for the
									// question, if any
		String presetAnswerLabel = prompt.getAnswerValue()!= null ? prompt.getAnswerValue().getDisplayText()
				: null;
		int count = 0;

		while (itr.hasMoreElements()) {
			SelectChoice choice = (SelectChoice) itr.nextElement();
			
			// check if the value is equal to the preset for this question
			if ((presetAnswerLabel != null)
					&& (choice.getValue().equals(presetAnswerLabel)))
				preselectionIndex = count;

			cg.append(choice.getCaption(), null);// add options to choice group

			count++;
		}
		this.append(cg);

		// set the selection to the preset value, if any
		if ((preselectionIndex > -1) && (preselectionIndex < cg.size()))
			cg.setSelectedIndex(preselectionIndex, true);

		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}

	}

	public IAnswerData getWidgetValue() {

		int selectedIndex = -1;
		Selection s = null;

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {
				selectedIndex = i;
				s = prompt.getSelectChoices().elementAt(selectedIndex).selection();
				break;
			}
		}

		return (selectedIndex == -1 ? null : new SelectOneData(s));
	}

}