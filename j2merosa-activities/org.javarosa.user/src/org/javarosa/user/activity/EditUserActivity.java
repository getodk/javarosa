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
package org.javarosa.user.activity;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;
import org.javarosa.user.utility.AddUserContext;
import org.javarosa.user.view.NewUserForm;

/**
 * @author Clayton Sims
 * @date Mar 3, 2009 
 *
 */
public class EditUserActivity implements IActivity, CommandListener {

	private IShell parent = null;
	public final Command CMD_SAVE = new Command("Save", Command.OK, 2);
	public final Command CMD_CANCEL = new Command("Exit",Command.EXIT, 2);
	public static final String COMMAND_KEY = "command";

	AddUserContext context;
	NewUserForm addUser = null;
	
	boolean success = false;
	
	public EditUserActivity (IShell p) {
		this.parent = p;
		success = false;
	}
	
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}


	public void destroy() {
		// TODO Auto-generated method stub

	}


	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void halt() {
		// TODO Auto-generated method stub

	}

	
	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	
	public void start(Context context) {
		this.context = new AddUserContext(context);
		//this.newuserForm = new NewUserForm(this, "Login");
		//this.loginScreen.setCommandListener(this);
		//this.loginScreen.loginButton.setItemCommandListener(this);       // set item command listener
		//parent.setDisplay(this, this.loginScreen);
		
		
		
		//take this out into an activity
		addUser = new NewUserForm("Edit User", this.context.getDecorator());
		addUser.setPasswordMode(this.context.getPasswordFormat());
		addUser.addCommand(CMD_SAVE);
		addUser.addCommand(CMD_CANCEL);
		addUser.setCommandListener(this);
		
		UserRMSUtility userRMS = (UserRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(UserRMSUtility.getUtilityName());
		User user = new User();
		try {
			userRMS.retrieveFromRMS(this.context.getCurrentUserID().intValue(), user);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeserializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		addUser.loadUser(user);
		
		parent.setDisplay(this, addUser);

	}
	
	public void commandAction(Command c,Displayable d) {
		if(!d.equals(addUser)) {
			if (!success) {
				parent.setDisplay(this, addUser);
			} else {
				System.out.println("About to return from activity");
				Hashtable returnArgs = new Hashtable();
				returnArgs.put(Constants.RETURN_ARG_KEY, addUser
						.getConstructedUser());
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
						returnArgs);
			}
		}
		
		if (c == this.CMD_SAVE)
		{
	    	//#style mailAlert
			final Alert successfulNewUser  = new Alert("User added","User added successfully",null,AlertType.CONFIRMATION);

			String answer = addUser.readyToSave();

    		if (answer.equals(""))	{///success

    			successfulNewUser.setCommandListener(this);
    			successfulNewUser.setTimeout(Alert.FOREVER);
    			
    			parent.setDisplay(this, new IView() {public Object getScreenObject() { return successfulNewUser;}});
    			success = true;
    		}
    		else if (answer.substring(0,10 ).equals("Username ("))///name already taken..
    		{

    	    	//#style mailAlert
    			final Alert nameTakenError  = new Alert("Problem adding User - name taken",
						answer, null,AlertType.ERROR);
    			nameTakenError.setCommandListener(this);
    			nameTakenError.setTimeout(Alert.FOREVER);
    			parent.setDisplay(this, new IView() {public Object getScreenObject() { return nameTakenError;}});
    		}
    		else if (answer.substring(0,9).equals("Please fi") )
    		{
    			System.out.println(answer.substring(9));
    	    	//#style mailAlert
    			final Alert noInputError  = new Alert("Problem adding User - no input",
						answer, null,AlertType.ERROR);
    			noInputError.setTimeout(Alert.FOREVER);
    			noInputError.setCommandListener(this);

    			parent.setDisplay(this, new IView() {public Object getScreenObject() { return noInputError;}});
    		}
    		else if (answer.substring(0,9).equals("Please re"))///password error
    		{
    			System.out.println(answer.substring(9));
    	    	//#style mailAlert
    			final Alert passwordMismatchError  = new Alert("Problem adding User - passwords don't match",
						answer, null,AlertType.ERROR);
    			passwordMismatchError.setTimeout(Alert.FOREVER);
    			passwordMismatchError.setCommandListener(this);

    			parent.setDisplay(this, new IView() {public Object getScreenObject() { return passwordMismatchError;}});

    		}

    	}	
		else if (c == this.CMD_CANCEL)
		{
			Hashtable returnArgs = new Hashtable();
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs );
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
