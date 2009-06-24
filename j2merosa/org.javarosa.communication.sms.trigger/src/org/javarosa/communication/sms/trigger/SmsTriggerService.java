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

package org.javarosa.communication.sms.trigger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;

/**
 * The SmsTriggerService is a background process which polls
 * for incoming SMS messages, attempts to identify messages
 * that should be consumed, and consumes them.
 * 
 * @author Clayton Sims
 *
 */
public class SmsTriggerService extends TimerTask implements MessageListener {
	
	private MessageConnection connection;
	private Vector triggers = new Vector();
	private int pending;
	
	private Timer t;
	
	public boolean start(String port) {
		String address = "sms://:" + port;
		System.out.println("Address: " + address);
		try {
			connection = (MessageConnection) Connector.open(address);
			connection.setMessageListener(this);
			pending = 0;
			
			t = new Timer();
			t.schedule(this,1000,1000);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Couldn't start Sms Trigger Service!");
			return false;
		}
		return true;
	}
	
	public boolean stop() {
		try {
			t.cancel();
			connection.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Problem stopping the SMS trigger service");
		}
		return true;
	}

	public void run() {
		synchronized (this) {
			if (pending > 0) {
				final Alert alert = new Alert("test@!", "test/2@d!", null, AlertType.ALARM);
				JavaRosaServiceProvider.instance().showView(new IView() {
					public Object getScreenObject() {
						return alert;
					}
				});
				try {
					Message mess = connection.receive();
					pending--;

					// received a message
					if (mess instanceof TextMessage) {
						TextMessage tmsg = (TextMessage) mess;
						for (Enumeration en = triggers.elements(); en.hasMoreElements();) {
							
							ISmsTrigger trigger = (ISmsTrigger) en.nextElement();
							if (trigger.isConsumable(tmsg)) {
								trigger.consume(tmsg);
							}
						}
					}
				} catch (IOException ioe) {
					System.out.println("Error in reading trigger text message");
					ioe.printStackTrace();
				}
			}
		}
	}	
	
	public void notifyIncomingMessage(MessageConnection connection) {
		if(connection == this.connection) {
			pending++;
		}
	}
	
	public void addTrigger(ISmsTrigger trigger) {
		triggers.addElement(trigger);
	}
}
