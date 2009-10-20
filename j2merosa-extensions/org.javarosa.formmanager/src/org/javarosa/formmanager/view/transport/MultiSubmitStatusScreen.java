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
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.view.ISubmitStatusObserver;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.impl.TransportMessageStatus;

/**
 * Note: This screen assumes that the model IDs provided will be sent in a more
 * or less sequential fashion.
 * 
 * @author Clayton Sims
 * @date Jan 11, 2009
 * 
 */
public class MultiSubmitStatusScreen extends Form implements
		ISubmitStatusObserver, CommandListener {

	private static final int REFRESH_INTERVAL = 1000;
	private static final int TIMEOUT = 180000;

	private StringItem msg;// displayed

	private int currentid;
	private int counter = 0;
	private Hashtable ids;
	private int failed = 0;

	/**
	 * @param listener
	 * @param modelIDs
	 */
	public MultiSubmitStatusScreen(CommandListener listener) {
		//#style submitPopup
		super(Localization.get("sending.status.title"));

		setCommandListener(listener);

		addCommand(new Command(Localization.get("menu.ok"), Command.OK, 1));

	}

	public void reinit(String[] ids) {
		deleteAll();
		setModelIDs(ids);
		setMessage();
		failed = 0;
	}

	/**
	 * Error situation - no data to send. when "sent unsent" is called with no forms
	 */
	public void reinitNodata() {
		deleteAll();
		setMessage(Localization.get("sending.status.none"));
		failed = 0;
	}

	private void setMessage(String s) {
		append(new Spacer(80, 0));

		this.msg = new StringItem(null, s);

		append(this.msg);

	}

	private void setMessage() {
		append(new Spacer(80, 0));
		if (this.ids.size() == 0)
			this.msg = new StringItem(null, "No forms to send");
		else
			this.msg = new StringItem(null, getCurrentDisplay());
		append(this.msg);

	}

	public void commandAction(Command c, Displayable d) {
	}

	/**
	 * @param status
	 */
	private void updateStatusDisplay(int status) {

		System.out.println("updateStatusDisplay status= " + status);
		this.counter += REFRESH_INTERVAL;

		switch (status) {
		//
		case TransportMessageStatus.QUEUED:
		case -1: {// TODO: what does -1 mean?
			String message = (this.counter < TIMEOUT ? getCurrentDisplay()
					: Localization.get("sending.status.long"));
			this.msg.setText(message);
			break;
		}
			// finished
		case TransportMessageStatus.SENT: {

			this.currentid++;
			this.msg.setText(getCurrentDisplay());

			break;
		}

			// problem occured
		case TransportMessageStatus.CACHED:
			//Move along to the next form.
			this.currentid++;
			failed++;
			//this.msg.setText(Locale.get("sending.status.failed"));
			break;

		// another problem
		default:
			// #debug error
			System.out.println("Unrecognised status from Transport Manager: "
					+ status);

			this.currentid++;
			failed++;
			this.msg.setText(Localization.get("sending.status.error"));
			break;
		}
		
		if(this.currentid == ids.size()) {
			constructFinalMessage();
		}

	}
	
	private void constructFinalMessage() {
		String message = "";
		if(failed > 0) { 
			message += Localization.get("sending.status.failures", new String[]{String.valueOf(failed)}) + "\n";
		}
		if(failed < this.ids.size()) {
			message+= Localization.get("sending.status.success") + " " + getServerResponse() + "\n";
		}
		if(failed == this.ids.size()) {
			message = Localization.get("sending.status.failed");
		}
		this.msg.setText(message);
	}

	/**
	 * @return
	 */
	private String getCurrentDisplay() {
		return Localization.get("sending.status.multi", new String[] {
				String.valueOf(currentid), 
				String.valueOf(ids.size()),
				String.valueOf(currentid + 1),
				String.valueOf(ids.size())
		});
	}

	/**
	 * @return
	 */
	public String getServerResponse() {
		//currently unused;
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.formmanager.view.ISubmitStatusScreen#destroy()
	 */
	public void destroy() {
		this.deleteAll();
	}

	public void setModelIDs(String[] modelIDs) {
		ids = new Hashtable();
		for(int i = 0 ; i < modelIDs.length; ++i) {
			ids.put(modelIDs[i], new Boolean(false));
		}
	}

	public void receiveError(String details) {
		destroy();
		StringItem failure = new StringItem("","");
    	failure.setText(Localization.get("sending.status.error") + ": " + details);
		this.append(failure);
	}

	public void onChange(TransportMessage message, String remark) {
		//updateStatusDisplay(message.getStatus());
		//This one doesn't imply a status change. Unclear that we want to update
		//based on it this way.
	}

	public void onStatusChange(TransportMessage message) {
		updateStatusDisplay(message.getStatus());
	}

}
