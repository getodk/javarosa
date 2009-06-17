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

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;
import org.javarosa.user.view.UserForm;

public class UserValidator {

	private UserForm view;
	private UserRMSUtility userRMS;

	public final static int OK = 0;
	public final static int USER_EXISTS = 1;
	public final static int USERORPASSWD_MISSING = 2;
	public final static int MISMATCHED_PASSWORDS = 3;

	public UserValidator(UserForm view) {
		this.view = view;
		this.userRMS = (UserRMSUtility) JavaRosaServiceProvider.instance()
				.getStorageManager().getRMSStorageProvider().getUtility(
						UserRMSUtility.getUtilityName());

	}

	/**
	 * @return
	 */
	public int validateEditUser() {
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
	 * returns true if the given username is already in existence within the RMS
	 * 
	 * @param username
	 * @return
	 */
	private boolean usernameExistsInRMS(String username) {

		User userInRMS = new User();

		int numberOfUsers = this.userRMS.getNumberOfRecords();

		for (int i = 0; i < numberOfUsers; i++) {
			try {

				this.userRMS.retrieveFromRMS(i + 1, userInRMS);

			} catch (Exception e) {
				e.printStackTrace();
				// nothing to do for user
			}
			if (userInRMS.getUsername().equalsIgnoreCase(username))
				return true;

		}

		return false;
	}

}
