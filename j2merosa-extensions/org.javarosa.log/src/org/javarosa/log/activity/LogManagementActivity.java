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

/**
 * 
 */
package org.javarosa.log.activity;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.log.FlatLogSerializer;
import org.javarosa.core.services.ServiceRegistry;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.log.properties.LogPropertyRules;
import org.javarosa.log.view.LogManagementView;
import org.javarosa.log.view.LogViewer;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009
 * 
 */
public class LogManagementActivity implements IActivity, CommandListener {

	private static final String CLEAR_LOGS = "Clear Logs";
	private static final String VIEW_LOGS = "View Logs";
	private static final String SEND_LOGS = "Send Logs";

	private static final Command EXIT = new Command("Back", Command.BACK, 0);

	private IShell shell;
	private LogManagementView manager;
	private LogViewer viewer;

	public LogManagementActivity() {
		// 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {

		this.manager = initView();
		this.viewer = new LogViewer();

		shell.setDisplay(this, this.manager);
	}

	/**
	 * @return
	 */
	private LogManagementView initView() {
		LogManagementView v = new LogManagementView();
		v.append(VIEW_LOGS, null);
		v.append(SEND_LOGS, null);
		v.append(CLEAR_LOGS, null);
		v.addCommand(EXIT);
		v.setCommandListener(this);
		return v;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command com, Displayable d) {
		if (d instanceof Alert) {
			shell.setDisplay(this, manager);
		}
		if (d == manager) {
			String action = manager.getString(manager.getSelectedIndex());
			if (action.equals(CLEAR_LOGS)) {
				clearLogs();
			}

			if (action.equals(VIEW_LOGS)) {
				viewLogs();
			}

			if (action.equals(SEND_LOGS)) {
				sendLogs();
			}
			if (com.equals(EXIT)) {
				shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
						new Hashtable());
			}
		} else if (d == viewer) {
			shell.setDisplay(this, manager);
		}
	}

	/**
	 * 
	 */
	private void sendLogs() {
		byte[] logData = ServiceRegistry.instance().getIncidentLogger()
				.serializeLogs(new FlatLogSerializer());
		ByteArrayPayload payload = new ByteArrayPayload(logData, "",
				IDataPayload.PAYLOAD_TYPE_TEXT);
		HttpTransportDestination destination = new HttpTransportDestination(
				ServiceRegistry.instance().getPropertyManager()
						.getSingularProperty(LogPropertyRules.LOG_SUBMIT_URL));
		try {
			ServiceRegistry.instance().getTransportManager().enqueue(
					payload,
					destination,
					ServiceRegistry.instance().getTransportManager()
							.getCurrentTransportMethod(), 0);
			// #style mailAlert
			final Alert sending = new Alert("Sending Started",
					"Log Sending Started", null, AlertType.ERROR);
			sending.setTimeout(Alert.FOREVER);
			IView successalert = new IView() {
				public Object getScreenObject() {
					return sending;
				}
			};
			shell.setDisplay(this, successalert);

		} catch (IOException e) {
			// #style mailAlert
			final Alert failure = new Alert("Send Failed",
					"Log sending failure", null, AlertType.ERROR);
			failure.setTimeout(Alert.FOREVER);
			IView successalert = new IView() {
				public Object getScreenObject() {
					return failure;
				}
			};
			shell.setDisplay(this, successalert);
		}
	}

	/**
	 * 
	 */
	private void viewLogs() {
		this.viewer.deleteAll();
		byte[] logData = ServiceRegistry.instance().getIncidentLogger()
				.serializeLogs(new FlatLogSerializer());
		this.viewer.loadLogs(new String(logData));
		this.viewer.setCommandListener(this);
		this.viewer.addCommand(EXIT);
		shell.setDisplay(this, this.viewer);
	}

	/**
	 * 
	 */
	private void clearLogs() {
		ServiceRegistry.instance().getIncidentLogger().clearLogs();
		// #style mailAlert
		final Alert success = new Alert("Logs Cleared",
				"Logs cleared succesfully", null, AlertType.CONFIRMATION);
		success.setTimeout(Alert.FOREVER);
		IView successalert = new IView() {
			public Object getScreenObject() {
				return success;
			}
		};
		shell.setDisplay(this, successalert);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api
	 * .ICommand)
	 */
	public void annotateCommand(ICommand command) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.shell = shell;
	}
}
