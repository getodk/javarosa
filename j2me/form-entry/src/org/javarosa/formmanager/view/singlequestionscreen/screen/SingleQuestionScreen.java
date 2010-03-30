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

import javax.microedition.lcdui.Graphics;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.Ticker;

public abstract class SingleQuestionScreen extends FramedForm {

	protected FormEntryPrompt prompt;
	protected IAnswerData answer;

	// GUI elements
	public static Command previousCommand;
	public static Command nextCommand;
	public static Command viewAnswersCommand;
	public static Command languageSubMenu;
	public static Command[] languageCommands;

	public static Command nextItemCommand = new Command(Localization
			.get("menu.Next"), Command.ITEM, 1);
	
	private String groupName = "";
	
	//#style button
	public StringItem nextItem = new StringItem(null, Localization
			.get("button.Next"), Item.BUTTON);

	public SingleQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt.getShortText(), style);
		this.prompt = prompt;
		this.groupName=groupName;
		this.createView();
		this.setUpCommands();
	}

	public abstract void createView();

	public abstract IAnswerData getWidgetValue();

	public void setHint(String helpText) {
		Ticker tick = new Ticker("HELP: " + helpText);
		this.setTicker(tick);
	}

	private void setUpCommands() {
		nextCommand = new Command(Localization.get("menu.Next"),
				Command.SCREEN, 0);
		previousCommand = new Command(Localization.get("menu.Back"),
				Command.SCREEN, 2);
		viewAnswersCommand = new Command(Localization.get("menu.ViewAnswers"),
				Command.SCREEN, 3);

		this.addCommand(previousCommand);
		this.addCommand(viewAnswersCommand);
		this.addCommand(nextCommand);
	}

	public void addNavigationWidgets() {
		this.append(nextItem);
		nextItem.setDefaultCommand(nextItemCommand); // add Command to Item.
		
		if(!((groupName==null)||(groupName.equals("")))){
			//#style groupName
			 StringItem groupNameTitle = new StringItem(null,groupName, Item.LAYOUT_EXPAND);
			 append(Graphics.BOTTOM, groupNameTitle);
			
		}
	}
	
	public void addLanguageCommands(String[] availableLocales)
	{
		languageSubMenu = new Command("Language", Command.SCREEN, 2);
		addCommand(languageSubMenu);
		
		languageCommands = new Command[availableLocales.length];
    	for (int i = 0; i < languageCommands.length; i++){
    		languageCommands[i] = new Command(availableLocales[i], Command.SCREEN, 3);
    		this.addSubCommand(languageCommands[i], languageSubMenu);
    	}
	}

	public void show() {
		J2MEDisplay.setView(this);
	}

}
