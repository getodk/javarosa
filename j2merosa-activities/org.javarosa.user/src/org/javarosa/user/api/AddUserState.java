/**
 * 
 */
package org.javarosa.user.api;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.user.api.transitions.AddUserStateTransitions;
import org.javarosa.user.model.User;
import org.javarosa.user.utility.IUserDecorator;
import org.javarosa.user.utility.UserValidator;
import org.javarosa.user.view.UserForm;

/**
 * @author ctsims
 *
 */
public class AddUserState implements State<AddUserStateTransitions>, CommandListener {

	private AddUserStateTransitions transitions;
	private UserForm view; 
	
	public final static Command CMD_SAVE = new Command(Localization.get("menu.Save"),
			Command.OK, 2);
	public final static Command CMD_CANCEL = new Command(Localization.get("menu.Exit"),
			Command.EXIT, 2);

	
	public AddUserState(String passwordFormat) {
		this(passwordFormat, null);
	}
	public AddUserState(String passwordFormat, IUserDecorator decorator) {
		this.view = new UserForm(Localization.get("activity.adduser.adduser"), decorator);
		this.view.setPasswordMode(passwordFormat);
		this.view.addCommand(CMD_SAVE);
		this.view.addCommand(CMD_CANCEL);
		this.view.setCommandListener(this);

	}
	
	public State<AddUserStateTransitions> enter(AddUserStateTransitions transitions) {
		 this.transitions = transitions;
		 return this;
	}

	public void start() {
		J2MEDisplay.getDisplay().setCurrent(view);
	}
	
	public void commandAction(Command c, Displayable arg1) {
		if(c.equals(CMD_SAVE)) {
			UserValidator validator = new UserValidator(view);
			int status = validator.validateNewUser();
			if(status == UserValidator.OK) {
				User u = validator.constructUser();
				//Save to RMS
				transitions.userAdded(u);
			} else {
				validator.handleInvalidUser(status, this);
			}
		} else if(c.equals(CMD_CANCEL)) {
			transitions.cancel();
		}
	}
}
