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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.singlequestionscreen.screen.SingleQuestionScreen;

import de.enough.polish.ui.Style;

/**
 * @author mel An extended SingleQuestionScreen that acquires the answer data
 *         for the question by capturing (and possibly processing) some data
 * 
 */
public abstract class AcquiringQuestionScreen extends SingleQuestionScreen {
	protected IAnswerData acquiredData;
	public Command acquireCommand;
	AcquireScreen acquireScreen;

	public AcquiringQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt, groupName,style);
		addAcquireCommand();

	}

	/**
	 * @return the command that is used to stop capturing and attempt to
	 *         transform the captured data into answer data
	 */
	public abstract Command getAcquireCommand();

	public void addAcquireCommand() {
		this.acquireCommand = getAcquireCommand();
		this.addCommand(acquireCommand);
	}

	protected void setAcquiredData(IAnswerData acquiredData) {
		this.acquiredData = acquiredData;
		updateDisplay();
	}

	/**
	 * Update the question screen with a representation of the acquired data
	 */
	protected abstract void updateDisplay();

	/**
	 * @param callingListener
	 *            the listener to which control will be returned
	 * @return the screen that will do the acquiring
	 */
	public abstract AcquireScreen getAcquireScreen(
			CommandListener callingListener);

}
