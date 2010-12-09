/**
 * 
 */
package org.javarosa.user.api;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.user.api.transitions.LoginTransitions;
import org.javarosa.user.model.User;
import org.javarosa.user.view.LoginForm;
import org.javarosa.utilities.media.MediaUtils;

import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.StringItem;

/**
 * @author ctsims
 *
 */
public class LoginController implements HandledCommandListener {
	
	protected LoginTransitions transitions;

	protected LoginForm view;
	
	protected Alert demoModeAlert = null;
	protected String[] extraText;
	
	public LoginController() {
		this(null, true);
	}
	public LoginController(String[] extraText, String passwordFormat, boolean showDemo) {
		this.extraText = extraText;
		view = new LoginForm(Localization.get("form.login.login"), this.extraText, showDemo);
		view.setCommandListener(this);
		view.setPasswordMode(passwordFormat);
	}
	
	public LoginController(String title, String image, String[] extraText, String passwordFormat, boolean showDemo) {
		this.extraText = extraText;
		view = new LoginForm(null, this.extraText, showDemo);
		view.setCommandListener(this);
		view.setPasswordMode(passwordFormat);
		
		if(image != null) {
			//#style loginImage?
			view.append(Graphics.TOP, new ImageItem(null, MediaUtils.getImage(image), ImageItem.LAYOUT_CENTER, ""));
		}
		
		if(title != null) {
			//#style loginTitle?
			view.append(Graphics.TOP, new StringItem(null, title));
		}
	}
	
	public LoginController(String[] extraText, boolean showDemo) {
		this(extraText,AddUserController.PASSWORD_FORMAT_NUMERIC,showDemo);
	}

	public void setTransitions (LoginTransitions transitions) {
		this.transitions = transitions;
	}
	
	public void start() {
		J2MEDisplay.setView(view);
	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}
	
	// listener for the display
	public void _commandAction(Command c, Displayable d) {
		if (c == LoginForm.CMD_CANCEL_LOGIN) {
			transitions.exit();
		} else if (c == LoginForm.CMD_LOGIN_BUTTON) {
			if (this.view.validateUser()) {
				transitions.loggedIn(view.getLoggedInUser());
				return;
			}
			performCustomUserValidation();

		}

		//#if javarosa.login.demobutton
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
		//#endif
	}
	
	
	// this method is to be overridden by users. 
	// handle errors here.
	protected void performCustomUserValidation() {
		J2MEDisplay.showError(Localization.get("activity.login.loginincorrect"), Localization.get("activity.login.tryagain"));
	}

}
