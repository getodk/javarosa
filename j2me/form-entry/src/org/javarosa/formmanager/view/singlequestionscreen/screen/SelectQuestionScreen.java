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

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.formmanager.view.FormElementBinding;

import de.enough.polish.ui.Style;

public class SelectQuestionScreen extends OneQuestionScreen {
	ChoiceGroup cg;

	public SelectQuestionScreen(FormElementBinding qDef, Style style) {
		super(qDef, style);
	}

	public void createView() {
		if (qDef.instanceNode.required) {
			// #style choiceGroup
			cg = new ChoiceGroup("*"
					+ ((QuestionDef) qDef.element).getLongText(),
					ChoiceGroup.MULTIPLE);
		} else {
			// #style choiceGroup
			cg = new ChoiceGroup(((QuestionDef) qDef.element).getLongText(),
					ChoiceGroup.MULTIPLE);
		}
		Enumeration itr = ((QuestionDef) qDef.element).getSelectItems().keys();// access
																				// choices
																				// directly
		int i = 0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			cg.append(label, null);// add options to choice group
			i++;
		}
		this.append(cg);
		this.addNavigationButtons();
		if (((QuestionDef) qDef.element).getHelpText() != null) {
			setHint(((QuestionDef) qDef.element).getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		Vector vs = new Vector();

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {
				QuestionDef q = (QuestionDef) qDef.element;
				Selection s = new Selection((String) q.getSelectItemIDs()
						.elementAt(i));
				s.attachQuestionDef(q);
				vs.addElement(s);
			}
		}

		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
}