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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.log.FlatLogSerializer;
import org.javarosa.core.services.IncidentLogger;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.log.view.LogManagementView;
import org.javarosa.log.view.LogViewer;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009
 * 
 */
public abstract class LogManagementState implements TrivialTransitions, State, CommandListener {

	private static final String CLEAR_LOGS = "Clear Logs";
	private static final String VIEW_LOGS = "View Logs";
	private static final String SEND_LOGS = "Send Logs";

	private static final Command EXIT = new Command("Back", Command.BACK, 0);

	private LogManagementView manager;
	private LogViewer viewer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start() {
		this.manager = initView();
		this.viewer = new LogViewer();

		J2MEDisplay.setView(this.manager);
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
			J2MEDisplay.setView(this.manager);
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
				done();
			}
		} else if (d == viewer) {
			J2MEDisplay.setView(this.manager);
		}
	}

	/**
	 * 
	 */
	private void sendLogs() {
		throw new RuntimeException("LogManagementState.sendLogs(): need to convert to new transport layer");
		
//		String logData = IncidentLogger._().serializeLogs(new FlatLogSerializer());
//		ByteArrayPayload payload = new ByteArrayPayload(logData.getBytes(), "",
//				IDataPayload.PAYLOAD_TYPE_TEXT);
//		HttpTransportDestination destination = new HttpTransportDestination(
//				PropertyManager._().getSingularProperty(LogPropertyRules.LOG_SUBMIT_URL));
//		try {
//			TransportManager._().enqueue(
//					payload,
//					destination,
//					TransportManager._().getCurrentTransportMethod(), 0);
//			
//			J2MEDisplay.showError("Sending Started", "Log Sending Started");
//		} catch (IOException e) {
//			J2MEDisplay.showError("Send Failed", "Log sending failure");
//		}
	}

	/**
	 * 
	 */
	private void viewLogs() {
		this.viewer.deleteAll();
		String logData = IncidentLogger._().serializeLogs(new FlatLogSerializer());
		this.viewer.loadLogs(logData);
		this.viewer.setCommandListener(this);
		this.viewer.addCommand(EXIT);
		J2MEDisplay.setView(this.viewer);
	}

	/**
	 * 
	 */
	private void clearLogs() {
		IncidentLogger._().clearLogs();
		
		J2MEDisplay.showError("Logs Cleared", "Logs cleared succesfully");
	}

}
