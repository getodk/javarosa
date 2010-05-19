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

import java.util.Date;

import javax.microedition.lcdui.DateField;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Style;

public class TimeQuestionScreen extends SingleQuestionScreen {
	protected DateField timePicker;

	public TimeQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt,groupName,style);
	}

	public void createView() {
		//#style textBox
		timePicker = new DateField(prompt.getShortText(),
				DateField.TIME);
		// set question
		if (prompt.isRequired())
			timePicker.setLabel("*"
					+ prompt.getLongText()); 
		else
			timePicker.setLabel(prompt.getLongText());

		// check if the field has already been filled in by default value- if so
		// display value
		IAnswerData answer = prompt.getAnswerValue();
		if ((answer != null) && (answer instanceof TimeData)) {
			timePicker.setDate((Date) (((TimeData) answer).getValue()));
		}

		this.append(timePicker);
		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {

		if (timePicker.getDate() != null) {
			return new TimeData(timePicker.getDate());
		} else {
			return null;
		}
	}

}