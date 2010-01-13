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

import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.formmanager.view.FormElementBinding;

import de.enough.polish.ui.Style;

public class TextQuestionScreen extends OneQuestionScreen {

	protected TextField tf;

	public TextQuestionScreen(FormElementBinding qDef,Style style) {
		super(qDef,style);
	}

	public void createView() {
		setHint("Type in your answer");
		// #style textBox
		tf = new TextField("", "", 200, TextField.ANY);
		if (qDef.instanceNode.required)
			tf.setLabel("*" + ((QuestionDef) qDef.element).getLongText()); // visual
																			// symbol
																			// for
																			// required
		else
			tf.setLabel(((QuestionDef) qDef.element).getLongText());

		IAnswerData answerData = qDef.instanceNode.getValue();
		if ((answerData != null) && (answerData instanceof StringData))
			tf.setString(((StringData) answerData).getDisplayText());

		this.append(tf);
		this.addNavigationButtons();
		if (((QuestionDef) qDef.element).getHelpText() != null) {
			setHint(((QuestionDef) qDef.element).getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		String s = tf.getString();
		return (s == null || s.equals("") ? null : new StringData(s));
	}

}