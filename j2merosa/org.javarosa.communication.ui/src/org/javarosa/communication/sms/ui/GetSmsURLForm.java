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

package org.javarosa.communication.sms.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.javarosa.communication.sms.SmsTransportDestination;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.transport.ITransportDestination;

public class GetSmsURLForm extends Form implements IView {

	public static final Command CMD_OK = new Command("OK", Command.OK, 1);
	public static final Command CMD_BACK = new Command("Back", Command.BACK, 1);
	
	TextField textField;
	
	public GetSmsURLForm(ITransportDestination preload) {
		super("Get New URL");
		// destinationUrl = shell.getAppProperty("destination-file");
		// TODO: put this back in when the property manager is
		// complete again
		textField = new TextField(
				"Please enter destination path + filename",
				((SmsTransportDestination)preload).getSmsAddress(), 140, TextField.ANY);

		append(textField);
		addCommand(CMD_OK);
		addCommand(CMD_BACK);
	}
	
	public String getDestination() {
		return textField.getString();
	}

	public Object getScreenObject() {
		return this;
	}

}
