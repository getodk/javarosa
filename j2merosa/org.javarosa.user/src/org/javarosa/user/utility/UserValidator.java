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
