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
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Style;

public class SelectMultiQuestionScreen extends SingleQuestionScreen {
	ChoiceGroup cg;

	public SelectMultiQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt,groupName,style);
	}

	public void createView() {
		if (prompt.isRequired()) {
			// #style choiceGroup
			cg = new ChoiceGroup("*"
					+ prompt.getLongText(null),
					ChoiceGroup.MULTIPLE);
		} else {
			// #style choiceGroup
			cg = new ChoiceGroup(prompt.getLongText(null),
					ChoiceGroup.MULTIPLE);
		}
		
		SelectMultiData answerValue = (SelectMultiData) prompt.getAnswerValue();
		Vector<String> selectionStrings = getSelectionStrings(answerValue);
		Vector<SelectChoice> choices = prompt.getSelectChoices();
		Enumeration itr = choices.elements();
		
		int i = 0;
		boolean[] selectedFlags = new boolean[choices.size()];
		while (itr.hasMoreElements()) {
			SelectChoice choice = (SelectChoice)itr.nextElement();
			selectedFlags[i] = selectionStrings.contains(this.prompt.getSelectChoiceText(choice));
			String label = this.prompt.getSelectChoiceText(choice);
			cg.append(label, null);
			i++;
		}

		cg.setSelectedFlags(selectedFlags);
		this.append(cg);
		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}
	}

	private Vector<String> getSelectionStrings(SelectMultiData answerValue) {
		Vector<Selection> selections = (Vector<Selection>) answerValue.getValue();
		Vector<String> strings = new Vector<String>();
		for (Selection selection : selections) {
			strings.addElement(prompt.getSelectionText(selection));
		}
		return strings;
	}

	public IAnswerData getWidgetValue() {
		Vector<Selection> vs = new Vector<Selection>();

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {
				Selection s = prompt.getSelectChoices().elementAt(i).selection();
				vs.addElement(s);
			}
		}

		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
}