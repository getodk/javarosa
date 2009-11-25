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
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.ui.GetURLForm;
import org.javarosa.communication.sms.SmsTransportMethod;
import org.javarosa.communication.ui.GetDestinationTransitions;
import org.javarosa.core.api.State;
import org.javarosa.core.services.TransportManager;
import org.javarosa.j2me.view.J2MEDisplay;

public abstract class SmsDestinationRetrievalState implements GetDestinationTransitions, State, CommandListener {
	
	GetSmsURLForm form;

	public void start() {
		form = new GetSmsURLForm(((SmsTransportMethod)TransportManager._().getTransportMethod(
				new SmsTransportMethod().getId())).getDefaultDestination());
		form.setCommandListener(this);
		J2MEDisplay.setView(form);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		if(arg0 == GetURLForm.CMD_OK) {
			this.entered(new HttpTransportDestination(form.getDestination()));
		} else {
			this.cancel();
		}
	}
}
