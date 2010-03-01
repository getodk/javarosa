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

package org.javarosa.formmanager.api;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.formmanager.api.transitions.HttpFetchTransitions;
import org.javarosa.formmanager.view.ProgressScreen;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.services.transport.senders.SenderThread;

public abstract class GetFormListHttpState implements State,CommandListener,TransportListener, HttpFetchTransitions{

	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);
	public final Command CMD_RETRY = new Command("Retry",Command.BACK, 1);
	private ProgressScreen progressScreen = new ProgressScreen("Searching","Please Wait. Contacting Server...",this);
	
	private String getListUrl; 
	private String credentials;
	
	private String requestPayload = "#";
	
	private SenderThread thread;

	public GetFormListHttpState() {
		
	}
	
	public abstract String getUrl();
	
	public abstract String getUserName();
	
	private void init(){
		getListUrl = getUrl(); 
		credentials = "?user=" + getUserName();
		requestPayload = credentials;
	}
	
	public void start() {
		progressScreen.addCommand(CMD_CANCEL);
		J2MEDisplay.setView(progressScreen);
		init();
		fetchList();
	}
	
	public void fetchList() {
		SimpleHttpTransportMessage message= new SimpleHttpTransportMessage(requestPayload,getListUrl+credentials);//send username and url
		message.setCacheable(false);
		
		try {
			thread = TransportService.send(message);
			thread.addListener(this);
		} catch (TransportException e) {
			fail("Error Downloading List! Transport Exception while downloading forms list " + e.getMessage());
		}
	}
	
	private void fail(String message) {
		progressScreen.setText(message);
		progressScreen.addCommand(CMD_RETRY);
	}

	public void commandAction(Command command, Displayable display) {
		
		if(display== progressScreen){
			if(command == CMD_CANCEL){
				cancel();
			}
			if(command == CMD_RETRY) {
				progressScreen = new ProgressScreen("Searching","Please Wait. Contacting Server...",this);
				progressScreen.addCommand(CMD_CANCEL);
				J2MEDisplay.setView(progressScreen);
				fetchList();
			}
		}
		
	}

	public void process(byte[] response) {
		//FIXME - resolve the responses to be received from the webserver
		if(response ==null){
			//TODO: I don't think this is even possible.
			fail("Null Response from server");
		}else if(response.equals("WebServerResponses.GET_LIST_ERROR")){
			fail("Get List Error from Server");
		}else if(response.equals("WebServerResponses.GET_LIST_NO_SURVEY")){
			fail("No survey error from server");
		}else{
			fetched();
		}
		
	}
	
	public void onChange(TransportMessage message, String remark) {
		progressScreen.setText(remark);
	}

	public void onStatusChange(TransportMessage message) {
		if(message.getStatus() == TransportMessageStatus.SENT) {
			//TODO: Response codes signal statuses?
			process(((SimpleHttpTransportMessage)message).getResponseBody());
		} else {
			fail("Transport Failure: " + message.getFailureReason());
		}
	}
}
