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

import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.view.IQuestionWidget;
import org.javarosa.formmanager.view.widgets.ExpandedWidget;
import org.javarosa.formmanager.view.widgets.IWidgetStyleEditable;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.Ticker;
import de.enough.polish.ui.UiAccess;

public class SingleQuestionScreen extends FramedForm implements ItemCommandListener, ItemStateListener, IQuestionWidget {

	protected FormEntryPrompt prompt;
	private Gauge progressBar;
    
	private IWidgetStyleEditable widget;
	protected IAnswerData answer;
	JrFormEntryController fec;

	// GUI elements
	public Command previousCommand;
	//public Command nextCommand;
	public Command viewAnswersCommand;
	public Command languageSubMenu;
	public Command[] languageCommands;
	
	Command[] itemCommandQueue;

	public static Command nextItemCommand = new Command(Localization
			.get("menu.Next"), Command.OK, 1);
	
	//#style button
	public StringItem nextItem = new StringItem(null, Localization
			.get("button.Next"), Item.BUTTON);
	
	public SingleQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(groupName, style);
		throw new RuntimeException("Deprecated!");
	}

	public SingleQuestionScreen(FormEntryPrompt prompt, String groupName, IWidgetStyleEditable widget, JrFormEntryController fec, Style style) {
		super(groupName, style);
		itemCommandQueue = new Command[1];
		this.prompt = prompt;
		this.widget = widget;
		this.fec = fec;
		this.setUpCommands();
		this.createView();
	}

	public void createView() {
		widget.initWidget(prompt, this.container);
		widget.refreshWidget(prompt, FormElementStateListener.CHANGE_INIT);
		addNavigationWidgets();
		attachWidget();
	}
	
	private void attachWidget () {
		Item item = widget.getInteractiveWidget();
		
		item.addCommand(nextItemCommand);
		item.setItemCommandListener(this);
		
		switch(widget.getNextMode()) {
		case ExpandedWidget.NEXT_ON_MANUAL:
			item.setDefaultCommand(nextItemCommand);
			break;
		case ExpandedWidget.NEXT_ON_ENTRY: 
			item.setItemStateListener(this);
			break;
		case ExpandedWidget.NEXT_ON_SELECT:
			item.setDefaultCommand(nextItemCommand);
			break;
		}

	}

	public IAnswerData getWidgetValue() {
		return widget.getData();
	}
	
	public void configureProgressBar(int cur, int total) {
		if(progressBar == null) {
			//#style progressbar
			progressBar = new Gauge(null, false, total, cur);
		} else {
			progressBar.setMaxValue(total);
			progressBar.setValue(cur);
		}
        append(Graphics.BOTTOM, progressBar);
	}

	public void setHint(String helpText) {
		Ticker tick = new Ticker("HELP: " + helpText);
		this.setTicker(tick);
	}

	private void setUpCommands() {
//		nextCommand = new Command(Localization.get("menu.Next"),
//				Command.SCREEN, 0);
		previousCommand = new Command(Localization.get("menu.Back"),
				Command.BACK, 2);
		viewAnswersCommand = new Command(Localization.get("menu.ViewAnswers"),
				Command.SCREEN, 3);

		this.addCommand(previousCommand);
		this.addCommand(viewAnswersCommand);
		this.addCommand(nextItemCommand);
	}

	public void addNavigationWidgets() {
		if(this.widget.getNextMode() != ExpandedWidget.NEXT_ON_MANUAL) {
			this.append(nextItem);
			nextItem.setDefaultCommand(nextItemCommand); // add Command to Item.
		}
//		if(!((groupName==null)||(groupName.equals("")))){
//			//#style groupName
//			 StringItem groupNameTitle = new StringItem(null,groupName, Item.LAYOUT_EXPAND);
//			 append(Graphics.BOTTOM, groupNameTitle);
//			
//		}
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

	public void itemStateChanged(Item item) {
		if(item.equals(widget.getInteractiveWidget())) {
			this.callCommandListener(nextItemCommand);
		}
	}
		
	public void commandAction(Command c, Item item) {
		if(loaded && (this.getKeyStates() & UiAccess.FIRE_PRESSED) != 0) {
			//we're still in the middle of input, delay the outcome until it's done.
			synchronized(itemCommandQueue) {
				itemCommandQueue[0] = c;
			}
		} else {
			//We didn't get this command until we're unloaded, so just go for it
			CommandListener listener = this.getCommandListener();
			listener.commandAction(c, this);
		}
	}

	boolean loaded = false;
	//int loadedKey;
	//We only want to handle paired key events, so releases without a press (generally
	//coming from a different screen) need to be absorbed.
	protected boolean handleKeyPressed(int keyCode, int gameAction) {
		if(fec.handleKeyEvent(keyCode)) { return true; }
		synchronized(itemCommandQueue) {
			this.getKeyStates();
			itemCommandQueue[0] = null;
		}
		if((this.getKeyStates() & UiAccess.FIRE_PRESSED) != 0) {
			loaded = true;
		}
		return super.handleKeyPressed(keyCode, gameAction);
	}

	protected boolean handleKeyReleased(int keyCode, int gameAction) {
		//Clear key states
		this.getKeyStates();
		loaded = false;
		synchronized(itemCommandQueue) {
			if(itemCommandQueue[0] != null) {
				CommandListener listener = this.getCommandListener();
				listener.commandAction(itemCommandQueue[0], this);
				itemCommandQueue[0] = null;
				return true;
			}
		}
		return super.handleKeyReleased(keyCode, gameAction);
	}

	public void refreshWidget(int changeFlags) {
		widget.refreshWidget(prompt, changeFlags);		
	}
	
	public void releaseMedia() {
		widget.releaseMedia();
	}

	public void releaseResources() {
		if(prompt != null) { 
			prompt.unregister();
			widget.reset();
		}
		super.releaseResources();
	}
}
