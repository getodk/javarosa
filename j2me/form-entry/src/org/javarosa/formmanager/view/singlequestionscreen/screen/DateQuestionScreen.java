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

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Style;

public class DateQuestionScreen extends SingleQuestionScreen {
	protected DateField datePicker;

	public DateQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt,groupName,style);
	}

	public void createView() {
		// #style textBox
		datePicker = new DateField(prompt.getShortText(null),
				DateField.DATE);
		
		if (prompt.isRequired())
			datePicker.setLabel("*"
					+ prompt.getLongText(null));
		else
			datePicker.setLabel(prompt.getLongText(null));

		// check if the field has already been filled in by default value- if so
		// display value
		IAnswerData answer = prompt.getAnswerValue();
		if ((answer != null) && (answer instanceof DateData)) {
			datePicker.setDate((Date) (((DateData) answer).getValue()));
		}
		this.append(datePicker);
		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		if (datePicker.getDate() != null) {
			return new DateData(datePicker.getDate());
		} else {
			return null;
		}
	}

}