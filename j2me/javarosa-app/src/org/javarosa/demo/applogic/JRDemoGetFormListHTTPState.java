package org.javarosa.demo.applogic;

import org.javarosa.formmanager.api.GetFormListHttpState;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import java.io.UnsupportedEncodingException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.log.FatalException;
import org.javarosa.demo.util.ProgressScreenFormDownload;
import org.javarosa.demo.util.SimpleHttpTransportMessageGet;
import org.javarosa.formmanager.api.transitions.HttpFetchTransitions;
import org.javarosa.formmanager.view.ProgressScreen;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.services.transport.senders.SenderThread;

public class JRDemoGetFormListHTTPState implements State,HandledCommandListener,TransportListener, HttpFetchTransitions
{
	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);
	public final Command CMD_RETRY = new Command("Retry",Command.BACK, 1);
	private ProgressScreenFormDownload progressScreen = new ProgressScreenFormDownload("Searching","Please Wait. Contacting Server...",this);
	
	private String getListUrl; 
	private String credentials;
	
	private String requestPayload = "#";
	
	private SenderThread thread;

	private String response;
	
	public JRDemoGetFormListHTTPState(String url) {
		this.getListUrl = url;
	}
	
	public void SetURL(String url)
	{
		getListUrl = url;
	}
	
	public String getUrl()
	{
		return getListUrl;
	}
	
	public String getUserName()
	{
		return "";
	}
	
	private void init(){

		if (getListUrl.indexOf("?")>=0) 
			credentials = "";
		else
			credentials = "";
			//credentials = "?user=" + getUserName();
		requestPayload = credentials;
	}
	
	public void start() {
		progressScreen.addCommand(CMD_CANCEL);
		J2MEDisplay.setView(progressScreen);
		init();
		fetchList();
	}
	
	
	public void fetchList() {
		SimpleHttpTransportMessageGet message= new SimpleHttpTransportMessageGet(requestPayload,getListUrl+credentials);//send username and url
		message.setCacheable(false);
		
		try {
			thread = TransportService.send(message);
			thread.addListener(this);
		} catch (TransportException e) {
			fail("Error Downloading List! Transport Exception while downloading forms list " + e.getMessage());
		}
	}
	
	private void fail(String message) {
//		progressScreen.setText(message);
//		progressScreen.addCommand(CMD_RETRY);
		JRDemoFormListState state = new JRDemoFormListState();
		state.start();
		state.bla();
		
	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command command, Displayable display) {
		if(display== progressScreen){
			if(command == CMD_CANCEL){
				cancel();
			}
			if(command == CMD_RETRY) {
				progressScreen = new ProgressScreenFormDownload("Searching","Please Wait. Contacting Server...",this);
				progressScreen.addCommand(CMD_CANCEL);
				J2MEDisplay.setView(progressScreen);
				fetchList();
			}
		}
		
	}

	public void process(byte[] response) {
		String sResponse = null;
		if (response != null) {
			try {
				sResponse = new String(response, "UTF-8");
				this.response = sResponse;
				System.out.print(sResponse);
			} catch (UnsupportedEncodingException e) {
				throw new FatalException("can't happen; utf8 must be supported", e);
			}
		}
		
		//FIXME - resolve the responses to be received from the webserver
		if(sResponse ==null){
			//TODO: I don't think this is even possible.
			fail("Null Response from server");
		}else if(sResponse.equals("WebServerResponses.GET_LIST_ERROR")){
			fail("Get List Error from Server");
		}else if(sResponse.equals("WebServerResponses.GET_LIST_NO_SURVEY")){
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
			process(((SimpleHttpTransportMessageGet)message).getResponseBody());
		} else {
			fail("Transport Failure: " + message.getFailureReason());
		}
	}

	public void cancel() {
		new JRDemoFormListState().start();
	}

	public byte[] fetched() {
		JRDemoRemoteFormListState jr = new JRDemoRemoteFormListState(this.response);
		jr.start();
		return null;
	}
	
	

}
