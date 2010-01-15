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

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.utility.SortedIndexSet;
import org.javarosa.formmanager.view.IFormEntryView;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.List;

public class FormViewScreen extends List implements IFormEntryView {

	private FormEntryModel model;
	// private FormViewManager parent;

	public SortedIndexSet indexHash;

	// GUI elements
	public static Command exitNoSaveCommand;
	public static Command exitSaveCommand;
	public static Command sendCommand;
	public static Command backCommand;

	public FormViewScreen(FormEntryModel model) {
		// #style View_All_Form
		super(model.getForm().getTitle(), List.IMPLICIT);
		this.model = model;
		createView();
		setUpCommands();
	}

	private void setUpCommands() {
		exitNoSaveCommand = new Command(Localization.get("menu.Exit"),
				Command.EXIT, 4);
		exitSaveCommand = new Command(Localization.get("menu.SaveAndExit"),
				Command.SCREEN, 4);
		sendCommand = new Command(Localization.get("menu.SendForm"),
				Command.SCREEN, 4);

		// next command is added on a per-widget basis
		this.addCommand(exitNoSaveCommand);
		// screen.addCommand(exitSaveCommand);

		// TODO: FIXME
		// if(!model.isReadOnly()){
		// this.addCommand(sendCommand);
		// }
	}

	protected void createView() {
		((List) this).deleteAll();
		indexHash = new SortedIndexSet();

		FormIndex index = FormIndex.createBeginningOfFormIndex();
		FormDef form = model.getForm();
		while (!index.isEndOfFormIndex()) {
			if (index.isInForm() && model.isRelevant(index)) {
				String line = "";
				IFormElement element = form.getChild(index);
				if (element instanceof GroupDef) {
					FormEntryCaption caption = model.getCaptionPrompt(index);
					line = "--" + caption.getLongText() + "--";
				} else {
					FormEntryPrompt prompt = model.getQuestionPrompt(index);
					if (prompt.isRequired()) {
						line += "*";
					}
					line += prompt.getLongText() + " => ";

					IAnswerData answerValue = prompt.getAnswerValue();
					if (answerValue != null) {
						line += answerValue.getDisplayText();
					}
				}
				System.out.println(line);
				((List) this).append(line, null);
				indexHash.add(index);// map list index to question index.
			}
			index = form.incrementIndex(index);
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void show() {
		createView();
	}
}
