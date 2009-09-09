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

import java.util.Enumeration;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.formmanager.activity.FormTransportActivity;

public class FormTransportMessageList extends List implements CommandListener {

	private FormTransportActivity activity;

	public FormTransportMessageList(CommandListener activity) {
		super(Localization.get("message.list.messages"), Choice.IMPLICIT);
		addCommand(FormTransportCommands.CMD_BACK);
		addCommand(FormTransportCommands.CMD_DETAILS);
		addCommand(FormTransportCommands.CMD_DELETEMSG);
		addCommand(FormTransportCommands.CMD_SEND_ALL_UNSENT);
		setSelectCommand(FormTransportCommands.CMD_DETAILS);
		setCommandListener(activity);
		this.activity = (FormTransportActivity) activity;

	}

	public void refresh(){
		deleteAll();
		populate();
	}
	/**
	 *
	 */
	private void populate() {
		Enumeration messages = JavaRosaServiceProvider.instance()
				.getTransportManager().getMessages();
		while (messages.hasMoreElements()) {
			TransportMessage message = (TransportMessage) messages
					.nextElement();

			StringBuffer listEntry = new StringBuffer();
			listEntry.append("ID: ").append(message.getRecordId());
			listEntry.append(" - ").append(message.statusToString());
			append(listEntry.toString(), null);
		}
	}

	public void commandAction(Command c, Displayable d) {

		// #debug debug
		System.out.println("command: " + c.getLabel() + " d: " + d.getTitle());

		if (c == FormTransportCommands.CMD_BACK) {
			this.activity.handleBackFromMessageList();
		}

		if (c == FormTransportCommands.CMD_DETAILS) {
			handleDetails();
		}

		if (c == FormTransportCommands.CMD_DELETEMSG) {
			handleDeleteMessage();
		}
		
		
		throw new RuntimeException("Command " + c.getLabel()
				+ " uncaught - display:" + d.getTitle());
	}

	/**
	 * 
	 */
	private void handleDeleteMessage() {
		int selected = getSelectedIndex();
		Enumeration messages = JavaRosaServiceProvider.instance()
				.getTransportManager().getMessages();
		TransportMessage message = (TransportMessage) elementAt(selected,
				messages);

		JavaRosaServiceProvider.instance().getTransportManager().deleteMessage(
				message.getRecordId());
		this.activity.showMessageList();
	}

	private Object elementAt(int index, Enumeration en) {
		int i = 0;
		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			if (i == index) {
				return o;
			}
			i++;
		}
		return null;
	}

	/**
	 * 
	 */
	private void handleDetails() {
		int selected = getSelectedIndex();
		Enumeration messages = JavaRosaServiceProvider.instance()
				.getTransportManager().getMessages();
		TransportMessage message = (TransportMessage) elementAt(selected,
				messages);
		if (message != null) {
			this.activity.handleTransportMessage(message);
		}
	}


}
