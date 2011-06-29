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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

public class TriggerQuestionScreen extends SingleQuestionScreen {
	StringItem display;

	public TriggerQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt,groupName,style);
	}

	public void createView() {
		String message = prompt.isRequired() ? "*" +  prompt.getLongText() : prompt.getLongText();
		//#style textBox
		display = new StringItem(message,null);
		
		this.append(display);
		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		return new StringData("OK");
	}
}