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

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;

import org.javarosa.core.api.IView;
import org.javarosa.core.services.locale.Localization;

public class SendNowSendLaterForm extends Form implements IView {
	private ChoiceGroup cg;

	public static final int SEND_NOW_DEFAULT = 0;
	public static final int SEND_LATER = 1;
	public static final int SEND_NOW_SPEC = 2;

	public SendNowSendLaterForm(CommandListener activity) {
		//#style submitPopup
		super(Localization.get("sending.view.submit"));

		//#style submitYesNo
		this.cg = new ChoiceGroup(Localization.get("sending.view.when"), Choice.EXCLUSIVE);

		// NOTE! These Indexes are optimized to be added in a certain
		// order. _DO NOT_ change it without updating the static values
		// for their numerical order.
		this.cg.append(Localization.get("sending.view.now"), null);
		this.cg.append(Localization.get("sending.view.later"), null);
		

		//TODO: Add this back in for admin users. Who took it out?
		//this.cg.append(Locale.get("sending.view.new"), null);// clients wont need to
		// see
		// this

		append(this.cg);

		append(new Spacer(80, 0));

		setCommandListener(activity);
		setItemStateListener((ItemStateListener)activity);
	}

	public int getCommandChoice() {
		return this.cg.getSelectedIndex();
	}

	public Object getScreenObject() {
		return this;
	}

}
