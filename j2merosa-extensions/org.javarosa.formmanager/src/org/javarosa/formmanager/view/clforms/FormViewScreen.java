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

package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.SortedIndexSet;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.IFormEntryView;

public class FormViewScreen extends List implements IFormEntryView {

	private FormEntryModel model;
	//private FormViewManager parent;

	public SortedIndexSet indexHash;

	// GUI elements
	public static Command exitNoSaveCommand;
	public static Command exitSaveCommand;
	public static Command sendCommand;
	public static Command backCommand;

	public FormViewScreen (FormEntryModel model) {
        //#style CL_Forms_Form
    	super(model.getForm().getTitle(),List.IMPLICIT);
    	this.model = model;
    	createView();
		setUpCommands();
	}

	private void setUpCommands() {
		exitNoSaveCommand = new Command(Localization.get("menu.Exit"), Command.EXIT, 4);
		exitSaveCommand = new Command(Localization.get("menu.SaveAndExit"), Command.SCREEN, 4);
		sendCommand = new Command(Localization.get("menu.SendForm"), Command.SCREEN, 4);

		//saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);

		// next command is added on a per-widget basis
		this.addCommand(exitNoSaveCommand);
		//screen.addCommand(exitSaveCommand);
		if(!model.isReadOnly()){
		this.addCommand(sendCommand);
		}
		//screen.addCommand(saveAndReloadCommand);
	}

	protected void createView() {

		//Check who's relevant and display
//		form.calculateRelevantAll();

		//first ensure clean gui
		((List) this).deleteAll();
		indexHash = new SortedIndexSet();

		for (FormIndex i = model.getForm().incrementIndex(FormIndex.createBeginningOfFormIndex());
			 i.compareTo(FormIndex.createEndOfFormIndex()) < 0;
			 i = model.getForm().incrementIndex(i)) {
			// Check if relevant
			if(model.isRelevant(i))
			{
				FormElementBinding bind = new FormElementBinding(null, i, model.getForm());
				
				String stringVal;
				// Get current value as STring
				IAnswerData  val = bind.getValue();
				//check for null answers
				if(val == null){
					stringVal = "";
				}
				else {
				stringVal = val.getDisplayText();
				}

				if(bind.instanceNode.required){
				// Append to list
				((List) this).append("*"+((QuestionDef)bind.element).getShortText()+" => "+stringVal,null);
				}
				else
				{
					((List) this).append(((QuestionDef)bind.element).getShortText()+" => "+stringVal,null);
				}
				indexHash.add(i);//map list index to question index.
			}
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void show() {
		createView();
	}
}
