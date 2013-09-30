package org.javarosa.user.api;

import org.javarosa.core.api.State;
import org.javarosa.user.api.transitions.EditUserTransitions;
import org.javarosa.user.model.User;

public abstract class EditUserState implements EditUserTransitions, State {

	protected User u;
	
	public EditUserState (User u) {
		this.u = u;
	}
	
	public void start () {
		EditUserController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected EditUserController getController () {
		return new EditUserController(CreateUserController.PASSWORD_FORMAT_NUMERIC, u);
	}
	
}
