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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.user.model.User;
import org.javarosa.user.utility.LoginContext;
import org.javarosa.user.utility.Terms;
import org.javarosa.user.view.LoginForm;

/**
 * Show the login form, and controls the login process, including error messages for invalid username/passwords
 *
 */
public class LoginActivity implements IActivity, CommandListener,
		ItemCommandListener {

	private LoginForm view = null;
	private IShell parent = null;

	public static final String COMMAND_KEY = "command";
	public static final String USER = "user";

	private LoginContext context;

	private String title;
 

	public LoginActivity(IShell p, String s) {
		this.parent = p;
		this.title = s;
	}

	public LoginActivity(IShell p) {
		this.parent = p;
	}

	public void start(Context context) {
		this.context = new LoginContext(context);
		this.view = initView();
		this.parent.setDisplay(this, this.view);
	}

	/**
	 * 
	 */
	private LoginForm initView() {
		LoginForm loginForm;
		if (this.title == null)
			loginForm = new LoginForm(this);
		else
			loginForm = new LoginForm(this, this.title);
		loginForm.setPasswordMode(this.context.getPasswordFormat());
		loginForm.setCommandListener(this);

		// listen to the password field and the login button also
		loginForm.getLoginButton().setItemCommandListener(this);
		loginForm.getPasswordField().setItemCommandListener(this);

		return loginForm;
	}

	/*
	 * listener for the String Item (login button)
	 * 
	 * @see
	 * javax.microedition.lcdui.ItemCommandListener#commandAction(javax.microedition
	 * .lcdui.Command, javax.microedition.lcdui.Item)
	 */
	public void commandAction(Command c, Item item) {
		if (c == LoginForm.CMD_LOGIN_BUTTON) {

			// user trying to login, must be validated..
			if (this.view.validateUser()) {
				userValidated();
				return;

			}
			showError(Terms.loginIncorrect, Terms.tryAgain);

		}

		// #if javarosa.login.demobutton
		else if (c == LoginForm.CMD_DEMO_BUTTON) {
			showError(Terms.demoMode, Terms.demoModeIntro, this);
		}
		// #endif

	}

	private void userValidated() {
		// user is validated, activity completed
		Hashtable returnArgs = new Hashtable();
		returnArgs.put(COMMAND_KEY, "USER_VALIDATED");
		returnArgs.put(USER, this.view.getLoggedInUser());
		this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
				returnArgs);
	}

	// listener for the display
	public void commandAction(Command c, Displayable d) {
		if (c == LoginForm.CMD_CANCEL_LOGIN) {
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(COMMAND_KEY, "USER_CANCELLED");
			this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					returnArgs);
		} else if (c == LoginForm.CMD_LOGIN_BUTTON) {
			if (this.view.validateUser()) {
				
				// TODO: why the alert here and not in the other listener?
				final Alert success = this.view.successfulLoginAlert();
				this.parent.setDisplay(this, new IView() {
					public Object getScreenObject() {
						return success;
					}
				});
				userValidated();
				return;
			}
			showError(JavaRosaServiceProvider.instance().localize("activity.login.loginincorrect"), JavaRosaServiceProvider.instance().localize("activity.login.tryagain"));

		}

		// #if javarosa.login.demobutton
		else if (c == LoginForm.CMD_DEMO_BUTTON) {
			showError(JavaRosaServiceProvider.instance().localize("activity.login.demomode"), JavaRosaServiceProvider.instance().localize("activity.login.demomode.intro"), this);
		} else if (d instanceof Alert) {
			// returning from demo mode warning popup
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(COMMAND_KEY, "USER_VALIDATED");
			User u = new User();
			u.setUsername(User.DEMO_USER); // NOTE: Using a user type as a
			// username also!
			u.setUserType(User.DEMO_USER);
			returnArgs.put(USER, u);
			this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					returnArgs);
		}
		// #endif
	}

	public Context getActivityContext() {
		return this.context;
	}

	private void showError(String s, String message) {
		showError(s, message, null);
	}

	private void showError(String s, String message, CommandListener cl) {
		//#style mailAlert
		final Alert alert = new Alert(s, message, null, AlertType.ERROR);
		alert.setTimeout(Alert.FOREVER);
		if (cl != null)
			alert.setCommandListener(cl);
		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return alert;
			}
		});
	}

	public void contextChanged(Context context) {
		Vector contextChanges = this.context.mergeInContext(context);

		Enumeration en = contextChanges.elements();
		while (en.hasMoreElements()) {
			String changedValue = (String) en.nextElement();
			if (changedValue == Constants.USER_KEY) {
				// update username somewhere
			}
		}
	}

	public void resume(Context context) {
		this.contextChanged(context);
		// Possibly want to check for new/updated forms
		JavaRosaServiceProvider.instance().showView(this.view);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}

	public void halt() {
		// do nothing?
	}

	public void destroy() {
		// do nothing?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api
	 * .ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException(
				"The Activity Class "
						+ this.getClass().getName()
						+ " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
