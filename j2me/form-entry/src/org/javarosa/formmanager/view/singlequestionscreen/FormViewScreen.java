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

package org.javarosa.formmanager.view.singlequestionscreen;




import org.javarosa.core.api.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.utility.SortedIndexSet;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.List;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;

public class FormViewScreen extends List {

	private FormEntryModel model;
	// private FormViewManager parent;

	private SortedIndexSet indexSet;

	// GUI elements
	public static Command exitNoSaveCommand;
	public static Command exitSaveCommand;
	public static Command sendCommand;
	public static Command backCommand;

	public FormViewScreen(FormEntryModel model) {
		// #style View_All_Form
		super("Form Overview", List.IMPLICIT);
		this.model = model;
		setUpCommands();
		createView();
	}

	private void setUpCommands() {
		exitNoSaveCommand = new Command(Localization.get("menu.Exit"),
				Command.EXIT, 4);
		exitSaveCommand = new Command(Localization.get("menu.SaveAndExit"),
				Command.SCREEN, 4);
		sendCommand = new Command(Localization.get("menu.SendForm"),
				Command.SCREEN, 4);

		// next command is added on a per-widget basis
		addCommand(exitNoSaveCommand);
		// screen.addCommand(exitSaveCommand);

		// TODO: FIXME
		// if(!model.isReadOnly()){
		// this.addCommand(sendCommand);
		// }
	}

	protected void createView() {
		indexSet = new SortedIndexSet();
		
		FormIndex index = FormIndex.createBeginningOfFormIndex();
		FormDef form = model.getForm();
		while (!index.isEndOfFormIndex()) {
			if (index.isInForm() && model.isRelevant(index)) {
				if (model.getEvent(index) == FormEntryController.QUESTION_EVENT){
					FormEntryPrompt prompt = model.getQuestionPrompt(index);
					String styleName = getStyleName(prompt);
					String line = prompt.getLongText() + " => ";

					IAnswerData answerValue = prompt.getAnswerValue();
					if (answerValue != null) {
						line += answerValue.getDisplayText();
					}
					Style style = styleName == null ? null : StyleSheet.getStyle(styleName);
					append(line, null, style);
					indexSet.add(index);
				}
			}
			index = form.incrementIndex(index);
		}
	}
	
	private String getStyleName(FormEntryPrompt prompt) {
		if (prompt.isRequired() && prompt.getAnswerValue() == null) {
			return Constants.STYLE_COMPULSORY;
		}
		return null;
	}

	public FormIndex getFormIndex() {
		int selectedIndex = getSelectedIndex();
		if (selectedIndex < 0) {
			return null;
		}
		FormIndex formIndex = (FormIndex) indexSet.get(selectedIndex);
		return formIndex;
	}
}
