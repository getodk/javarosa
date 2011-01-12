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
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.CustomChoiceGroup;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.Style;

public class SelectOneQuestionScreen extends SingleQuestionScreen implements ItemStateListener {
	protected CustomChoiceGroup cg;
	protected boolean nextOnSelect;

	public SelectOneQuestionScreen(FormEntryPrompt prompt, String groupName, Style style, boolean nextOnSelect) {
		super(prompt,groupName,style);
		this.nextOnSelect = nextOnSelect;
	}

	public void createView() {
		if (prompt.isRequired()) {
			//#style choiceGroup
			cg = new CustomChoiceGroup("*"
					+ prompt.getLongText(),
					ChoiceGroup.EXCLUSIVE, true);
		} else {
			//#style choiceGroup
			cg = new CustomChoiceGroup(prompt.getLongText(),
					ChoiceGroup.EXCLUSIVE, true);
		}

		Enumeration itr = (prompt.getSelectChoices().elements());
		
		int preselectionIndex = -1; // index of the preset value for the
									// question, if any
		
		String xmlValue = null;
		if(prompt.getAnswerValue() != null) {
			xmlValue = ((Selection)new SelectOneData().cast(prompt.getAnswerValue().uncast()).getValue()).xmlValue;
		}
		int count = 0;

		while (itr.hasMoreElements()) {
			SelectChoice choice = (SelectChoice) itr.nextElement();
			
			// check if the value is equal to the preset for this question
			if ((xmlValue != null) && (choice.getValue().equals(xmlValue))) {
				preselectionIndex = count;
			}

			cg.append(prompt.getSelectChoiceText(choice), null);// add options to choice group

			count++;
		}
		this.append(cg);

		// set the selection to the preset value, if any
		if ((preselectionIndex > -1) && (preselectionIndex < cg.size())) {
			cg.setSelectedIndex(preselectionIndex, true);
		}

		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}
		
		cg.setItemStateListener(this);
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

	public void itemStateChanged(Item item) {
		if(nextOnSelect) {
			this.handleCommand(this.nextCommand);
		}
	}
}