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

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Style;

public class DecimalQuestionScreen extends TextQuestionScreen {
	
	IAnswerData template;

	public DecimalQuestionScreen(FormEntryPrompt prompt, String groupName, Style style, IAnswerData template) {
		super(prompt,groupName,style);
		this.template = template;
	}

	public void createView() {
		super.createView();
		tf.setConstraints(TextField.DECIMAL);
	}

	public IAnswerData getWidgetValue() {
		String s = tf.getString();

		if (s == null || s.equals(""))
			return null;

		String def = "-999999999";
		try {
			return template.cast(new UncastData(s));

		} catch (NumberFormatException nfe) {
			System.err.println("Non-numeric data in numeric entry field!");
			return template.cast(new UncastData(def));
		} catch (IllegalArgumentException iae) {
			System.err.println("malformed entry into numeric entry field!");
			return template.cast(new UncastData(def));
		} 
	}

}