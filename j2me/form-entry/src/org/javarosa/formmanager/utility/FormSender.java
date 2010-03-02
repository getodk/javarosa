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

package org.javarosa.formmanager.utility;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.formmanager.view.ISubmitStatusObserver;
import org.javarosa.formmanager.view.transport.FormTransportSubmitStatusScreen;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.formmanager.view.transport.MultiSubmitStatusScreen;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.senders.SenderThread;

/**
 * Managing sending forms, both single forms, and multiple forms together
 * 
 */
public class FormSender implements Runnable {

	/**
	 * true if many forms will be sent at once
	 * 
	 * TODO eliminate - look at vector to determine single or multiple
	 */
	private boolean multiple;

	TransportMessage message;

	/**
	 * The data to be sent when multiple = true
	 */
	private Vector messages;
	
	private ISubmitStatusObserver observer;
	
	private FormTransportViews views;

	/**
	 * @param shell
	 * @param activity
	 */
	public FormSender(FormTransportViews views, TransportMessage message) {
		this.views = views;
		this.message = message;
	}
	
	/**
	 * @param shell
	 * @param activity
	 */
	public FormSender(FormTransportViews views, Vector messages) {
		this.views = views;
		this.messages = messages;
	}
	
	public void sendData() {
		// #debug debug
		System.out.println("Sending data .. multiple=" + multiple);
		
		initDisplay();
		
		new Thread(this).start();
	}

	/**
	 * @param mainMenu
	 * @throws IOException
	 */
	private void sendSingle() throws TransportException {

		if (this.message == null)
			throw new RuntimeException(
					"null data when trying to send single data");

		// #debug debug
		System.out.println("Sending single datum, serialized id="
				+ this.message.getCacheIdentifier());

		send(message);
	}
	
	private void initDisplay() {

		if (this.multiple) {
			MultiSubmitStatusScreen s = views.getMultiSubmitStatusScreen();

			boolean noData = (this.messages == null)
					|| (this.messages.size() == 0);

			if (noData) {
				s.reinitNodata();
			} else {

				String idsStr = "";
				// #debug debug
				System.out.println("Multi send");
				String[] ids = new String[this.messages.size()];

				for (int i = 0; i < ids.length; ++i) {
					ids[i] = ((TransportMessage) this.messages.elementAt(i))
							.getCacheIdentifier();
					idsStr += " " + ids[i];
				}

				s.reinit(ids);

				// #debug debug
				System.out.println("ids: " + idsStr);
			}

			J2MEDisplay.setView(s);
			setObserver(s);
		}
		else {
			FormTransportSubmitStatusScreen statusScreen = views.getSubmitStatusScreen();
			statusScreen.reinit(this.message.getCacheIdentifier());
			J2MEDisplay.setView(statusScreen);
			setObserver(statusScreen);
		}
	}

	/**
	 * @throws IOException
	 */
	private void sendMultiData() throws TransportException {
		
		boolean noData = (this.messages == null)
		|| (this.messages.size() == 0);
		
		if (!noData) {
			for (Enumeration en = this.messages.elements(); en
					.hasMoreElements();) {
				TransportMessage message = (TransportMessage) en.nextElement();
				send(message);
			}
		}

	}

	private void send(TransportMessage message) throws TransportException {
		SenderThread thread = TransportService.send(message);
		thread.addListener(observer);
	}

	// ----------- getters and setters
	
	public void setObserver(ISubmitStatusObserver o) {
		this.observer = o;
	}

	public boolean isMultiple() {
		return this.multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public void run() {
		if (this.multiple) {
			try {
				sendMultiData();
			}
			catch(TransportException e) {
				e.printStackTrace();
				if(observer != null) {
					observer.receiveError(e.getMessage());
				}
			}

		} else {
			try{ 
				sendSingle();
			}
			catch(TransportException e) {
				e.printStackTrace();
				if(observer != null) {
					observer.receiveError(e.getMessage());
				}
			}
		}
	}

}
