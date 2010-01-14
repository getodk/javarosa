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

package org.javarosa.formmanager.view.singlequestionscreen.acquire;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import org.javarosa.core.model.data.IAnswerData;

/**
 * @author mel
 * 
 *         A screen that acquires the answer data for a question
 * 
 */
public abstract class AcquireScreen extends Form
		implements CommandListener /* , IFormEntryView */ {

	private AcquiringQuestionScreen questionScreen;
	private CommandListener listenerToReturnTo;
	public Command cancelCommand;
	private Command setCallingScreenDataCommand;

	/**
	 * @param title
	 *            the screen title
	 * @param questionScreen
	 *            the question screen to which the acquiredanswer data will be
	 *            returned
	 * @param listenerToReturnTo
	 *            the listener to which control will be returned once the
	 *            acquiring process has terminated. this can happen becuase data
	 *            is successfully acquired or returned, or because acquiring is
	 *            cancelled
	 */
	public AcquireScreen(String title, AcquiringQuestionScreen questionScreen,
			CommandListener listenerToReturnTo) {
		super(title);
		this.questionScreen = questionScreen;
		this.listenerToReturnTo = listenerToReturnTo;

		createView();
		addCommands();
		setCommandListener(this);
	}

	/**
	 * Add initial view items to form
	 */
	protected abstract void createView();

	/**
	 * command handler for screen-specific commands
	 * 
	 * @param command
	 * @param arg1
	 */
	protected abstract void handleCustomCommand(Command command,
			Displayable arg1);

	/**
	 * Add generic commands
	 */
	private void addCommands() {
		cancelCommand = new Command("Cancel", Command.CANCEL, 9);
		this.addCommand(cancelCommand);
		setCallingScreenDataCommand = getSetCallingScreenDataCommand();
		this.addCommand(setCallingScreenDataCommand);
	}

	/**
	 * @return the command that is used to stop capturing, do any processing and
	 *         attempt to return the acquired data as answer data
	 */
	protected abstract Command getSetCallingScreenDataCommand();

	/**
	 * @return the question screen for which data is being acquired
	 */
	public AcquiringQuestionScreen getQuestionScreen() {
		return questionScreen;
	}

	protected abstract IAnswerData getAcquiredData();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.microedition.lcdui.CommandListener#commandAction(javax.microedition
	 * .lcdui.Command, javax.microedition.lcdui.Displayable)
	 * 
	 * Command handler for generic acquisition commands
	 */
	public void commandAction(Command command, Displayable arg1) {

		if (command == cancelCommand) {
			cleanUp();
			listenerToReturnTo.commandAction(command, arg1);

		} else if (command == setCallingScreenDataCommand) {
			IAnswerData data = this.getAcquiredData();
			if (data != null) {
				questionScreen.setAcquiredData(data);
				commandAction(cancelCommand, this);
			}

		} else {
			handleCustomCommand(command, arg1);
		}

	}

	/**
	 * Cleans up the resources used to do the acquiring (e.g. video player)
	 */
	protected abstract void cleanUp();
}
