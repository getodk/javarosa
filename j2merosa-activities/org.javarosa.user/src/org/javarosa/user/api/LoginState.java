/**
 * 
 */
package org.javarosa.user.api;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

import org.javarosa.core.api.State;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.user.api.transitions.LoginStateTransitions;
import org.javarosa.user.model.User;
import org.javarosa.user.view.LoginForm;

/**
 * @author ctsims
 *
 */
public class LoginState implements State<LoginStateTransitions>, CommandListener {
	
	LoginStateTransitions transitions;
	LoginForm view;
	
	public LoginState() {
		
	}

	public void enter(LoginStateTransitions transitions) {
		this.transitions = transitions;
		view = new LoginForm();
		view.setCommandListener(this);
		view.setPasswordMode(AddUserState.PASSWORD_FORMAT_NUMERIC);
	}
	
	public void start() {
		J2MEDisplay.getDisplay().setCurrent(view);
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
				transitions.loggedIn(view.getLoggedInUser());
				return;

			}
			showError(Localization.get("activity.login.loginincorrect"), Localization.get("activity.login.tryagain"), this);

		}

		// #if javarosa.login.demobutton
		else if (c == LoginForm.CMD_DEMO_BUTTON) {
			showError(Localization.get("activity.login.demomode"), Localization.get("activity.login.demomode.intro"), this);
		}
		// #endif

	}

	// listener for the display
	public void commandAction(Command c, Displayable d) {
		if (c == LoginForm.CMD_CANCEL_LOGIN) {
			transitions.exit();
		} else if (c == LoginForm.CMD_LOGIN_BUTTON) {
			if (this.view.validateUser()) {
				transitions.loggedIn(view.getLoggedInUser());
				return;
			}
			showError(Localization.get("activity.login.loginincorrect"), Localization.get("activity.login.tryagain"));

		}

		// #if javarosa.login.demobutton
		else if (c == LoginForm.CMD_DEMO_BUTTON) {
			showError(Localization.get("activity.login.demomode"), Localization.get("activity.login.demomode.intro"), this);
		} else if (d instanceof Alert) {
			// returning from demo mode warning popup
			User u = new User();
			u.setUsername(User.DEMO_USER); // NOTE: Using a user type as a
			// username also!
			u.setUserType(User.DEMO_USER);
			transitions.loggedIn(u);
		}
		// #endif
	}

	private void showError(String s, String message) {
		showError(s, message, this);
	}

	private void showError(String s, String message, CommandListener cl) {
		J2MEDisplay.showError(s, message);
	}

}
