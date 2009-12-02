/**
 * 
 */
package org.javarosa.user.api;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.user.api.transitions.LoginTransitions;
import org.javarosa.user.model.User;
import org.javarosa.user.view.LoginForm;

/**
 * @author ctsims
 *
 */
public class LoginController implements CommandListener {
	
	LoginTransitions transitions;

	LoginForm view;
	
	Alert demoModeAlert = null;
	private String[] extraText;
	
	public LoginController() {
		this(null);
	}
	
	public LoginController(String[] extraText) {
		this.extraText = extraText;
		view = new LoginForm(Localization.get("form.login.login"), this.extraText);
		view.setCommandListener(this);
		view.setPasswordMode(AddUserController.PASSWORD_FORMAT_NUMERIC);
	}

	public void setTransitions (LoginTransitions transitions) {
		this.transitions = transitions;
	}
	
	public void start() {
		J2MEDisplay.setView(view);
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
			J2MEDisplay.showError(Localization.get("activity.login.loginincorrect"), Localization.get("activity.login.tryagain"));

		}

		// #if javarosa.login.demobutton
		else if (c == LoginForm.CMD_DEMO_BUTTON) {
			demoModeAlert = J2MEDisplay.showError(Localization.get("activity.login.demomode"), Localization.get("activity.login.demomode.intro"), null, this);
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
			J2MEDisplay.showError(Localization.get("activity.login.loginincorrect"), Localization.get("activity.login.tryagain"));

		}

		// #if javarosa.login.demobutton
		else if (c == LoginForm.CMD_DEMO_BUTTON) {
			demoModeAlert = J2MEDisplay.showError(Localization.get("activity.login.demomode"), Localization.get("activity.login.demomode.intro"), null, this);
		} else if (d == demoModeAlert) {
			// returning from demo mode warning popup
			User u = new User();
			u.setUsername(User.DEMO_USER); // NOTE: Using a user type as a
			// username also!
			u.setUserType(User.DEMO_USER);
			transitions.loggedIn(u);
		}
		// #endif
	}

}
