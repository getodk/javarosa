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

package org.javarosa.user.view;

import javax.microedition.lcdui.Command;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.user.api.AddUserController;
import org.javarosa.user.model.User;

import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class LoginForm extends FramedForm {

	private final static int DEFAULT_COMMAND_PRIORITY = 1;

	// #if javarosa.login.demobutton
	private StringItem demoButton;
	public final static Command CMD_DEMO_BUTTON = new Command(Localization.get("menu.Demo"),
			Command.ITEM, DEFAULT_COMMAND_PRIORITY);
	// #endif

	public final static Command CMD_CANCEL_LOGIN = new Command(Localization.get("menu.Exit"),
			Command.SCREEN, DEFAULT_COMMAND_PRIORITY);
	public final static Command CMD_LOGIN_BUTTON = new Command(Localization.get("menu.Login"),
			Command.ITEM, DEFAULT_COMMAND_PRIORITY);

	private StringItem loginButton;
	private TextField usernameField;
	private TextField passwordField;
	private IStorageUtility users;
	private User loggedInUser;

	private String[] extraText;
	
	public LoginForm() {
		//#style loginView
		super(Localization.get("form.login.login"));
		init();
	}

	/**
	 * @param title
	 */
	public LoginForm(String title) {
		//#style loginView
		super(title);
		init();
	}

	/**
	 * 
	 * @param title
	 * @param extraText
	 */
	public LoginForm(String title, String[] extraText) {
		//#style loginView
		super(title);
		this.extraText = extraText;
		init();
	}

	/**
	 * @param loginActivity
	 */
	private void init() {
		this.users = StorageManager.getStorage(User.STORAGE_KEY);

		boolean recordsExist = this.users.getNumRecords() > 0;

		// #debug debug
		System.out.println("records in RMS:" + recordsExist);

		User tempUser = new User();
		tempUser.setUsername("");// ? needed

		if (recordsExist) {
			int highestID = -1;
			IStorageIterator ui = users.iterate();
			while (ui.hasMore()) {
				int userID = ui.nextID();
				if (highestID == -1 || userID > highestID)
					highestID = userID;
			}
			
			tempUser = (User)users.read(highestID);

		}
		initLoginControls(tempUser.getUsername());

		showVersions();

	}

	/**
	 * @param username
	 */
	private void initLoginControls(String username) {

		// create the username and password input fields
		this.usernameField = new TextField(Localization.get("form.login.username"), username, 50,
				TextField.ANY);
		this.passwordField = new TextField(Localization.get("form.login.password"), "", 10,
				TextField.PASSWORD);

		// TODO:what this?
		addCommand(CMD_CANCEL_LOGIN);

		append(this.usernameField);
		append(this.passwordField);

		// set the focus on the password field
		this.focus(this.passwordField);
		this.passwordField.setDefaultCommand(CMD_LOGIN_BUTTON);

		// add the login button
		this.loginButton = new StringItem(null, Localization.get("form.login.login"), Item.BUTTON);
		append(this.loginButton);
		this.loginButton.setDefaultCommand(CMD_LOGIN_BUTTON);

		// #if javarosa.login.demobutton
		this.demoButton = new StringItem(null, Localization.get("menu.Demo"), Item.BUTTON);
		append(this.demoButton);
		this.demoButton.setDefaultCommand(CMD_DEMO_BUTTON);
		// #endif

		// put the extra text if it's been set
		if(this.extraText != null) {
			for (int i = 0; i < extraText.length; i++) {
				append(this.extraText[i]);
			}
		}
	}

	/**
	 * 
	 */
	private void showVersions() {
		//#if javarosa.login.showbuild
		//String ccv = (String) this.parent.getActivityContext().getElement(
		//		CTX_COMMCARE_VERSION);
		//String ccb = (String) this.parent.getActivityContext().getElement(
		//		CTX_COMMCARE_BUILD);
		//String jrb = (String) this.parent.getActivityContext().getElement(
		//		CTX_JAVAROSA_BUILD);

		//if (ccv != null && !ccv.equals("")) {
		//	this.append(Localization.get("form.login.commcareversion") + " " + ccv);
		//}
		//if ((ccb != null && !ccb.equals(""))
		//		&& (jrb != null && !jrb.equals(""))) {
		//	this.append(Localization.get("form.login.buildnumber") + " " + ccb + "-" + jrb);
		//}
		//#endif
	}

	/**
	 * 
	 * After login button is clicked, activity asks form to validate user
	 * 
	 * @return
	 */
	public boolean validateUser() {
		boolean superUserLogin = false;
		//#ifdef superuser-login.enable:defined
		//#if superuser-login.enable
		//#    superUserLogin = true;
		//#endif
		//#endif
		
		String usernameEntered = this.usernameField.getString().trim();
		String passwordEntered = this.passwordField.getString().trim();

		IStorageIterator ui = users.iterate();
		while (ui.hasMore()) {
			User u = (User)ui.nextRecord();
			
			String xName = u.getUsername();
			String xPass = u.getPassword();
			String xType = u.getUserType();
			
			if (xPass.equals(passwordEntered) && (	
					xName.equalsIgnoreCase(usernameEntered) ||
					(superUserLogin && xType.equals(User.ADMINUSER))
				)) {
				setLoggedInUser(u);
				return true;
			}
		}

		return false;

	}

	/**
	 * @param passwordMode
	 */
	public void setPasswordMode(String passwordMode) {
		if (AddUserController.PASSWORD_FORMAT_NUMERIC.equals(passwordMode)) {
			this.passwordField.setConstraints(TextField.PASSWORD
					| TextField.NUMERIC);
		} else if (AddUserController.PASSWORD_FORMAT_ALPHA_NUMERIC
				.equals(passwordMode)) {
			this.passwordField.setConstraints(TextField.PASSWORD);
		}
	}

	public String getPassWord() {
		return this.passwordField.getString();

	}

	public String getUserName() {
		return this.usernameField.getString();

	}

	public User getLoggedInUser() {
		return this.loggedInUser;
	}

	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
	}

	public Object getScreenObject() {
		return this;
	}

	public TextField getPasswordField() {
		return this.passwordField;
	}

	// ------------------------

	public StringItem getLoginButton() {
		return this.loginButton;
	}

}
