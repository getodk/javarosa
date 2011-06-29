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

import org.javarosa.core.services.Logger;
import org.javarosa.formmanager.view.ISubmitStatusObserver;
import org.javarosa.formmanager.view.transport.FormTransportSubmitStatusScreen;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.senders.SenderThread;

/**
 * droos 2/23/10: i'm removing the multi-send capability of this class. it is handled
 * elsewhere by code that is actually used and maintained (SendAllUnsentState in the
 * commcare-core repo). the way this handled multi-send was also terrible; it spawned
 * threads for each payload and tried to send them all simultaneously.
 * 
 * since thread management is now handled in the transport layer itself, this class does
 * not need to be Runnable
 * 
 */

/**
 * Managing sending forms, both single forms, and multiple forms together
 * 
 */
public class FormSender {

	TransportMessage message;
	private ISubmitStatusObserver observer;
	FormTransportSubmitStatusScreen view;

	/**
	 * @param shell
	 * @param activity
	 */
	public FormSender(FormTransportSubmitStatusScreen view, TransportMessage message) {
		this.view = view;
		this.message = message;
	}
	

	public void setObserver(ISubmitStatusObserver o) {
		this.observer = o;
	}

	public void sendData() {
		if (this.message == null)
			throw new RuntimeException(
					"null data when trying to send single data");

		try{ 
			//#debug debug
			System.out.println("Sending single datum, serialized id="
					+ this.message.getCacheIdentifier());

			SenderThread thread = TransportService.send(message);
			view.reinit(message.getCacheIdentifier());
			thread.addListener(observer);
		}
		catch(TransportException e) {
			Logger.exception("FormSender.sendData", e);
			e.printStackTrace();
			if(observer != null) {
				observer.receiveError(e.getMessage());
			}
		}
	}

}
