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

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.services.locale.Localization;

public class FormTransportViews {
	// ----------- subviews
//	private FormTransportMainMenu mainMenu;
//	private FormTransportMessageList messageList;
	private TextBox loggingTextBox;
//	private FormTransportMessageDetailBox messageDetailTextBox;
	private SendNowSendLaterForm sendNowSendLater;
	private FormTransportSubmitStatusScreen submitStatusScreen;
	private MultiSubmitStatusScreen multiSubmitStatusScreen;

	public FormTransportViews(CommandListener observer, ItemStateListener stateListener) {
		super();
//		this.mainMenu = constructMainMenu(observer);
//		this.messageList = new FormTransportMessageList(observer);
		this.loggingTextBox = createLoggingTextBox();
//		this.messageDetailTextBox = new FormTransportMessageDetailBox(observer);
		this.sendNowSendLater = new SendNowSendLaterForm(observer,stateListener);
		this.submitStatusScreen = new FormTransportSubmitStatusScreen(observer);

		this.multiSubmitStatusScreen = new MultiSubmitStatusScreen(observer);

	}

	private TextBox createLoggingTextBox() {
		TextBox box = new TextBox(Localization.get("message.log"), null, 1000,
				TextField.UNEDITABLE);
		box.addCommand(FormTransportCommands.CMD_BACK);
		return box;

	}
//
//	private FormTransportMainMenu constructMainMenu(CommandListener observer) {
//		Vector mainMenuItems = FormTransportMainMenu.getMenuItems();
//		String[] elements = new String[mainMenuItems.size()];
//		mainMenuItems.copyInto(elements);
//		return new FormTransportMainMenu(observer,
//				Localization.get("menu.transport"), Choice.IMPLICIT, elements, null);
//
//	}

	public void destroyStatusScreen() {
		this.submitStatusScreen.destroy();
	}

//	public List getMainMenu() {
//		return this.mainMenu;
//	}
//
//	public FormTransportMessageList getMessageList() {
//		return this.messageList;
//	}
//
//	public TextBox getLoggingTextBox() {
//		return this.loggingTextBox;
//	}
//
//	public TextBox getMessageDetailTextBox() {
//		return this.messageDetailTextBox;
//	}

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
