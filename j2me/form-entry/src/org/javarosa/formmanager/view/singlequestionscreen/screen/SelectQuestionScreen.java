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

public class SelectQuestionScreen extends SingleQuestionScreen {
	ChoiceGroup cg;

	public SelectQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt,groupName,style);
	}

	public void createView() {
		if (prompt.isRequired()) {
			// #style choiceGroup
			cg = new ChoiceGroup("*"
					+ prompt.getLongText(),
					ChoiceGroup.MULTIPLE);
		} else {
			// #style choiceGroup
			cg = new ChoiceGroup(prompt.getLongText(),
					ChoiceGroup.MULTIPLE);
		}
		Enumeration itr = (prompt.getSelectChoices().elements());
		
		int i = 0;
		while (itr.hasMoreElements()) {
			SelectChoice choice = (SelectChoice)itr.nextElement();
			String label = choice.getCaption();
			cg.append(label, null);// add options to choice group
			i++;
		}
		this.append(cg);
		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		Vector vs = new Vector();

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {

				Selection s = new Selection(prompt.getSelectChoices().elementAt(i));
				
				vs.addElement(s);
			}
		}

		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
}