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
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.formmanager.activity.FormTransportActivity;
import org.javarosa.formmanager.view.ISubmitStatusScreen;

public class FormTransportSubmitStatusScreen extends Form implements
		ISubmitStatusScreen, CommandListener {
	private int modelID = -1;
	private StringItem msg;
	private Command okCommand;
	private Timer timer;
	private int counter = 0;

	private FormTransportActivity activity;

	private static final int REFRESH_INTERVAL = 1000;
	private static final int TIMEOUT = 60000;

	public FormTransportSubmitStatusScreen(CommandListener activity) {
		//#style submitPopup
		super(Localization.get("sending.status.title"));
		setCommandListener(this);

		
		this.activity = (FormTransportActivity) activity;
	}

	
	public void reinit(int modelId){
		
		setModelID(modelId);
		this.okCommand = new Command(Localization.get("menu.ok"), Command.OK, 1);
		this.msg = new StringItem(null, Localization.get("sending.status.going"));

		addCommand(this.okCommand);
		append(new Spacer(80, 0));
		append(this.msg);

		initTimer();
	}
	
	
	public void commandAction(Command c, Displayable d) {
		//destroy status screen was here
		this.activity.returnComplete();
	}

	private void initTimer() {
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {
			public void run() {
				updateStatus();
			}
		}, REFRESH_INTERVAL, REFRESH_INTERVAL);
	}

	/**
	 * 
	 */
	public void updateStatus() {
		int status = JavaRosaServiceProvider.instance().getTransportManager()
				.getModelDeliveryStatus(this.modelID, false);
		updateStatus(status);
	}

	/**
	 * @param status
	 */
	public void updateStatus(int status) {
		this.counter += REFRESH_INTERVAL;

		if (status != TransportMessage.STATUS_NEW)
			this.timer.cancel();

		String message;
		switch (status) {
		case TransportMessage.STATUS_NEW:
			message = (this.counter < TIMEOUT ? Localization.get("sending.status.going")
					: Localization.get("sending.status.long"));
			break;
		case TransportMessage.STATUS_DELIVERED:
			message = Localization.get("sending.status.success");// + "  " + getServerResponse();
			break;
		case TransportMessage.STATUS_FAILED:
			message = Localization.get("sending.status.failed");
			break;
		default:
			message = Localization.get("sending.status.error");
			break;
		}

		this.msg.setText(message);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.ISubmitStatusScreen#receiveMessage(int)
	 */
	public void receiveMessage(int message, String details) {
		//TODO: Handle messages specifically, if any other than error occur
		switch(message) {
		    default:
		    	//TODO: Specific sending error?
				destroy();
				StringItem failure = new StringItem("","");
		    	failure.setText(Localization.get("sending.status.error") + ": " + details);
				this.append(failure);
		}
	}


	public void destroy() {
		deleteAll();
		this.timer.cancel();
	}

	/**
	 * @return
	 */
	public String getServerResponse() {
		Enumeration messages = JavaRosaServiceProvider.instance()
				.getTransportManager().getMessages();
		String receipt = "";

		TransportMessage response = (TransportMessage) messages.nextElement();
		receipt = new String(response.getReplyloadData()); // this does not seem
		// terribly robust

		return receipt;
	}

	public Object getScreenObject() {
		return this;
	}

	public void setModelID(int modelID) {
		this.modelID = modelID;
	}

}
