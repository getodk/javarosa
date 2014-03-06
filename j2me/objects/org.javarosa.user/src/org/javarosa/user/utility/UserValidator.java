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

package org.javarosa.user.utility;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.CommandListener;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.user.model.User;
import org.javarosa.user.view.UserForm;

public class UserValidator {

	private UserForm view;
	private IStorageUtility users;

	public final static int OK = 0;
	public final static int USER_EXISTS = 1;
	public final static int USERORPASSWD_MISSING = 2;
	public final static int MISMATCHED_PASSWORDS = 3;

	public UserValidator(UserForm view) {
		this.view = view;
		this.users = StorageManager.getStorage(User.STORAGE_KEY);
	}

	/**
	 * @return
	 */
	public int validateUserEdit() {
		final String username = this.view.getUsername();
		final String password = this.view.getPassword();
		final String passwordConfirmed = this.view.getPasswordConfirmation();

		if ((username.length() == 0) || (password.length() == 0)) {

			return USERORPASSWD_MISSING;
		}

		if (!(password.equals(passwordConfirmed))) {

			return MISMATCHED_PASSWORDS;
		}

		return OK;
	}

	/**
	 * @return
	 */
	public int validateNewUser() {
		final String username = this.view.getUsername();
		final String password = this.view.getPassword();
		final String passwordConfirmed = this.view.getPasswordConfirmation();

		boolean usernameExists = usernameExistsInRMS(username);
		if (usernameExists) {

			return USER_EXISTS;

		}

		if ((username.length() == 0) || (password.length() == 0)) {

			return USERORPASSWD_MISSING;
		}

		if (!(password.equals(passwordConfirmed))) {

			return MISMATCHED_PASSWORDS;
		}
		
		return OK;

	}

	/**
	 * 
	 * 
	 * 
	 * @param status
	 */
	public void handleInvalidUser(int status, CommandListener listener) {

		String s = Localization.get("activity.adduser.problem");

		if (status == UserValidator.USER_EXISTS) {
			s += ": " + Localization.get("activity.adduser.problem.nametaken", new String[]{this.view.getUsername()});
		}

		if (status == UserValidator.USERORPASSWD_MISSING) {
			s += ": " + Localization.get("activity.adduser.problem.emptyuser");
		}

		if (status == UserValidator.MISMATCHED_PASSWORDS) {
			s += ": " + Localization.get("activity.adduser.problem.mismatchingpasswords");
		}
		
		J2MEDisplay.showError(null,s);
	}


	public User constructUser() {
		User user;

		final String username = this.view.getUsername();
		final String password = this.view.getPassword();
		int userid = this.view.getUserId();

		if (!this.view.adminRightsSelected())
			user = new User(username, password, String.valueOf(userid));
		else
			user = new User(username, password, String.valueOf(userid), User.ADMINUSER);

		if (this.view.getDecorator() != null) {
			String[] elements = this.view.getDecorator()
					.getPertinentProperties();
			for (int i = 0; i < elements.length; ++i) {
				user.setProperty(elements[i], this.view.getMetaFields()[i]
						.getString());
			}
		}
		user.setUuid(PropertyUtils.genGUID(25));
		return user;
	}
	
	/**
	 * 
	 * returns true if the given username is already in existence within the RMS
	 * 
	 * @param username
	 * @return
	 */
	private boolean usernameExistsInRMS(String username) {

		IStorageIterator ui = users.iterate();
		while (ui.hasMore()) {
			User u = (User)ui.nextRecord();
			if (u.getUsername().equalsIgnoreCase(username))
				return true;
		}

		return false;
	}

}
