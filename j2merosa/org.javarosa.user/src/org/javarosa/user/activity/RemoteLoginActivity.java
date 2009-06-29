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

package org.javarosa.user.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.TransportManager;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.Observable;
import org.javarosa.core.util.Observer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;
import org.javarosa.user.view.LoginForm;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class RemoteLoginActivity implements IActivity,CommandListener,ItemCommandListener,Observer{
	private LoginForm logScr;
	private TransportMessage message;
	private TransportManager transportManager;
	private IShell parent;
	private Context context;
	private ByteArrayInputStream bin;
	private KXmlParser parser = new KXmlParser();
	//private ProgressScreen progressScreen;
	public static final String COMMAND_KEY = "command";
	public static final String USER = "user";
	public static final String PROFILE = "profile";
	public static final String TYPE = "logintype";

	// private javax.microedition.lcdui.Alert success;
	// private javax.microedition.lcdui.Alert fail;
	// private javax.microedition.lcdui.Alert alertdialog;


	public RemoteLoginActivity(IShell parent, String title) {
		this.parent =parent;
		//progressScreen = new ProgressScreen("Login In","Please Wait. Contacting Server...",this);
	}

	
	public void contextChanged(Context globalContext) {
		context.mergeInContext(globalContext);

	}

	
	public void destroy() {
		if(transportManager!=null){
			//transportManager.closeSend();
			transportManager = null;

		}

	}

	
	public Context getActivityContext() {
		return context;
	}

	
	public void halt() {
		// TODO Auto-generated method stub

	}

	
	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	
	public void setShell(IShell shell) {
		this.parent = shell;

	}

	
	public void start(Context context) {
		this.context=context;
		logScr = new LoginForm(this);
		logScr.setCommandListener(this);
		logScr.getLoginButton().setItemCommandListener(this);
		parent.setDisplay(this, logScr);


	}

	
	public void commandAction(Command command, Displayable display) {
		
		if(command==logScr.CMD_CANCEL_LOGIN){
			System.out.println("Clicks on the hem Exit");
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(COMMAND_KEY, "USER_CANCELLED");
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,returnArgs);
		}
		
	}


	public void commandAction(Command command, Item item) {
		if(command==logScr.CMD_LOGIN_BUTTON){
				if(validateUser()){
					
					final Alert success = logScr.successfulLoginAlert();
					parent.setDisplay(this, new IView() {public Object getScreenObject() {return success;}});
					
					Hashtable returnArgs = new Hashtable();
					returnArgs.put(COMMAND_KEY, "USER_VALIDATED");
					returnArgs.put(USER, logScr.getLoggedInUser());
					returnArgs.put(TYPE, "LOCAL_AUTH");
					parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
							returnArgs);

				}
				else if(logScr.getUserName()==""||logScr.getPassWord()==""){
					final Alert fail = new Alert("Error", "Enter Username and Password", null, AlertType.ERROR);
					parent.setDisplay(this, new IView() {public Object getScreenObject() {return fail;}});
					Hashtable returnArgs = new Hashtable();
					returnArgs.put(COMMAND_KEY, "USER_NOT_VALIDATED");
					parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,returnArgs);
					
				}else{
					//parent.setDisplay(this, progressScreen);
					String loginUrl = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(HttpTransportProperties.AUTH_URL_PROPERTY);
					   loginUrl+="?user="+logScr.getUserName()+"&pass="+logScr.getPassWord();
					   System.out.println(loginUrl); 
					ITransportDestination requestDest= new HttpTransportDestination(loginUrl);
					message = new TransportMessage();
					message.setPayloadData(new ByteArrayPayload("".getBytes(),null,IDataPayload.PAYLOAD_TYPE_TEXT)); //might have to change
					message.setDestination(requestDest);
					message.addObserver(this);
					transportManager = (TransportManager)JavaRosaServiceProvider.instance().getTransportManager();
					transportManager.send(message, TransportMethod.HTTP_GCF);
					
				}
		}

	}

	public boolean validateUser(){
		boolean validLogin = false;
		UserRMSUtility userRMS = logScr.getUserRMS();
		User discoveredUser = new User();
		String usernameStr = logScr.getUserName();
		int index = 1;

		while (index <= userRMS.getNumberOfRecords() )
		{
			try
			{
				userRMS.retrieveFromRMS(index, discoveredUser);
			}
			catch (IOException ioe) {
				System.out.println(ioe);
			}
			catch (DeserializationException uee) {
				System.out.println(uee);
			}
			if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
				break;

			index++;
		}

		if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr)){
			if (discoveredUser.getPassword().equals(logScr.getPassWord()))
			{
				System.out.println("login valid");
				validLogin = true;
				logScr.setLoggedInUser(discoveredUser);
			}
		}
		return validLogin;

	}

	
	public void update(Observable observable, Object arg) {
		byte[] data = (byte[])arg;
		//String response;
		String printme = new String(data).trim();
		System.out.println("SERVER SAYS: "+printme);
		
		bin = new ByteArrayInputStream(data);
		
		//parse info
		Hashtable temp = new Hashtable();
		
		try {
			parser.setInput(new InputStreamReader(bin));
			parseProfile(parser, temp);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//extract server response only for now
		String response = (String) temp.get("response");

		if(response ==null){
			final Alert alertdialog = new Alert("Web Service Error", "No response from server", null, AlertType.ERROR);
			alertdialog.setTimeout(1000);
			parent.setDisplay(this, new IView() {public Object getScreenObject() {return alertdialog;}});
			parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
		}else if(response.equalsIgnoreCase("OK")){
			final Alert success = logScr.successfulLoginAlert();
			parent.setDisplay(this, new IView() {public Object getScreenObject() {return success;}});
			saveUser();
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(COMMAND_KEY, "USER_VALIDATED");
			returnArgs.put(USER, logScr.getLoggedInUser());
			returnArgs.put(PROFILE, temp); //return the whole hashtable profile for now?
			returnArgs.put(TYPE, "REMOTE_AUTH");
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					returnArgs);

		}else if(response.equals("authenticationerror")){
			final Alert fail = new Alert("Error", "Invalid uID or Password: "+ response, null, AlertType.ERROR);
			//fail.setTimeout(1000);
			parent.setDisplay(this, new IView() {public Object getScreenObject() {return fail;}});
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(COMMAND_KEY, "USER_NOT_VALIDATED");
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,returnArgs);
			
		}else if(response.equals("notfound")){
			final Alert fail = new Alert("Error", "Server error: " +response, null, AlertType.ERROR);
			fail.setTimeout(1000);
			parent.setDisplay(this, new IView() {public Object getScreenObject() {return fail;}});
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(COMMAND_KEY, "USER_NOT_VALIDATED");
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,returnArgs);
			
		}

	}
	
	public void saveUser(){
		UserRMSUtility userRMS = logScr.getUserRMS();
		User discoveredUser = new User();
		discoveredUser.setUsername(logScr.getUserName());
		discoveredUser.setPassword(logScr.getPassWord());
		discoveredUser.setUserType(User.STANDARD);//make all users standard users? what if they are admin on the server?
		logScr.setLoggedInUser(discoveredUser);
		userRMS.writeToRMS(discoveredUser);
		
	}
	
public void parseProfile(KXmlParser parser, Hashtable formInfo) throws XmlPullParserException{
		
		try {
		//	boolean inItem = false;
			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, "profile");
			while( parser.nextTag() != XmlPullParser.END_TAG ){
				//parser file names
				parser.require(XmlPullParser.START_TAG, null, null);
				
				String name = parser.getName();
				//String url = parser.getAttributeValue(null, "url");
				String text = parser.nextText();
System.out.println("<"+name+">"+text);				
				if(name.equals("response") || name.equals("posturl") || name.equals("geturl") || name.equals("viewtype") )
					{
					//inItem = true;
					//items.addElement(text);
					formInfo.put(name,text);
					}
				//else
					//inItem = false;

				//parser.require(XmlPullParser.END_TAG, null, "form");
			}
			parser.require(XmlPullParser.END_TAG, null, "profile");
			
			parser.next();
			parser.require(XmlPullParser.END_DOCUMENT, null, null);

		} catch (IOException e) {
			// TODO: handle exception
			System.out.println("XML parser error");
			e.printStackTrace();

		}
	}


public void annotateCommand(ICommand command) {//to check what effect this has if left unimplemented
	// TODO Auto-generated method stub
	
}


}
