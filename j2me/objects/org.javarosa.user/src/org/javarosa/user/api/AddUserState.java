package org.javarosa.user.api;

import org.javarosa.core.api.State;
import org.javarosa.user.api.transitions.AddUserTransitions;

public abstract class AddUserState implements AddUserTransitions, State {

	public void start () {
		AddUserController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected AddUserController getController () {
		return new AddUserController(AddUserController.PASSWORD_FORMAT_NUMERIC);
	}
	
}
