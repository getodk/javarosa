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

import java.util.Timer;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.view.ISubmitStatusObserver;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledTimerTask;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportMessageStatus;

public class FormTransportSubmitStatusScreen extends Form implements
	ISubmitStatusObserver, HandledCommandListener {
	private String cacheId = null;
	private StringItem msg;
	private Command okCommand;
	private Timer timer;
	private int counter = 0;

	private TransportResponseProcessor responder;
	
	private static final int REFRESH_INTERVAL = 1000;
	private static final int TIMEOUT = 60000;

	public FormTransportSubmitStatusScreen(CommandListener listener, TransportResponseProcessor responder) {
		//#style submitPopup
		super(Localization.get("sending.status.title"));
		setCommandListener(listener);
		this.responder = responder;
		
		this.msg = new StringItem(null, Localization.get("sending.status.going"));
		append(new Spacer(80, 0));
		append(this.msg);
	}

	
	public void reinit(String cacheId){
		setCacheId(cacheId);
		this.okCommand = new Command(Localization.get("menu.ok"), Command.OK, 1);

		addCommand(this.okCommand);

		initTimer();
	}
	
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command c, Displayable d) {

	}

	private void initTimer() {
		this.timer = new Timer();
		this.timer.schedule(new HandledTimerTask() {
			public void _run() {
				updateStatus();
			}
		}, REFRESH_INTERVAL, REFRESH_INTERVAL);
	}

	/**
	 * 
	 */
	public void updateStatus() {
		TransportMessage message = TransportService.retrieve(cacheId);
		
		updateStatus(message);
	}

	/**
	 * @param status
	 */
	public void updateStatus(TransportMessage transportMessage) {
		this.counter += REFRESH_INTERVAL;

		int status = transportMessage.getStatus();
		if (status != TransportMessageStatus.QUEUED) {
			this.timer.cancel();
		}

		String message;
		switch (status) {
		case TransportMessageStatus.QUEUED:
			message = (this.counter < TIMEOUT ? Localization.get("sending.status.going")
					: Localization.get("sending.status.long"));
			break;
		case TransportMessageStatus.SENT:
			message = getResponseMessage(transportMessage);
			break;
		case TransportMessageStatus.CACHED:
			message = Localization.get("sending.status.failed");
			break;
		default:
			message = Localization.get("sending.status.error");
			break;
		}

		this.msg.setText(message);
	}

	private String getResponseMessage (TransportMessage message) {
		if (responder != null) {
			return responder.getResponseMessage(message);
		} else {
			return Localization.get("sending.status.success");
		}
	}
	
	public void destroy() {
		deleteAll();
		this.timer.cancel();
	}

	public void setCacheId(String cacheId) {
		this.cacheId = cacheId;
	}


	public void onChange(TransportMessage message,String remark) {
		updateStatus(message);
	}


	public void onStatusChange(TransportMessage message) {
		updateStatus(message);
	}


	public void receiveError(String details) {
		this.timer.cancel();
		this.msg.setText(Localization.get("sending.status.error") + " details");
	}

}
