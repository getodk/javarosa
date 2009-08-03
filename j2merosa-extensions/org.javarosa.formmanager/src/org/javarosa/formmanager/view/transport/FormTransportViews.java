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

package org.javarosa.formmanager.view.transport;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.activity.FormTransportActivity;

public class FormTransportViews {
	FormTransportActivity activity;
	// ----------- subviews
	private FormTransportMainMenu mainMenu;
	private FormTransportMessageList messageList;
	private TextBox loggingTextBox;
	private FormTransportMessageDetailBox messageDetailTextBox;
	private SendNowSendLaterForm sendNowSendLater;
	private FormTransportSubmitStatusScreen submitStatusScreen;
	private MultiSubmitStatusScreen multiSubmitStatusScreen;

	public FormTransportViews(FormTransportActivity activity) {
		super();
		this.activity = activity;
		this.mainMenu = constructMainMenu();
		this.messageList = new FormTransportMessageList(this.activity);
		this.loggingTextBox = createLoggingTextBox();
		this.messageDetailTextBox = new FormTransportMessageDetailBox(
				this.activity);
		this.sendNowSendLater = new SendNowSendLaterForm(this.activity);
		this.submitStatusScreen = new FormTransportSubmitStatusScreen(
				this.activity);

		this.multiSubmitStatusScreen = new MultiSubmitStatusScreen(this.activity);

	}

	private TextBox createLoggingTextBox() {
		TextBox box = new TextBox(Localization.get("message.log"), null, 1000,
				TextField.UNEDITABLE);
		box.addCommand(FormTransportCommands.CMD_BACK);
		return box;

	}

	private FormTransportMainMenu constructMainMenu() {
		Vector mainMenuItems = FormTransportMainMenu.getMenuItems();
		String[] elements = new String[mainMenuItems.size()];
		mainMenuItems.copyInto(elements);
		return new FormTransportMainMenu(this.activity,
				Localization.get("menu.transport"), Choice.IMPLICIT, elements, null);

	}

	public void destroyStatusScreen() {
		this.submitStatusScreen.destroy();
	}

	public FormTransportActivity getActivity() {
		return this.activity;
	}

	public List getMainMenu() {
		return this.mainMenu;
	}

	public FormTransportMessageList getMessageList() {
		return this.messageList;
	}

	public TextBox getLoggingTextBox() {
		return this.loggingTextBox;
	}

	public TextBox getMessageDetailTextBox() {
		return this.messageDetailTextBox;
	}

	public SendNowSendLaterForm getSendNowSendLaterScreen() {
		return this.sendNowSendLater;
	}

	public FormTransportSubmitStatusScreen getSubmitStatusScreen() {
		return this.submitStatusScreen;
	}

	public MultiSubmitStatusScreen getMultiSubmitStatusScreen() {
		return multiSubmitStatusScreen;
	}

}
