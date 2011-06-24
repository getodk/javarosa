package org.javarosa.user.api;

import org.javarosa.core.api.State;
import org.javarosa.user.api.transitions.AddUserTransitions;

public abstract class CreateUserState implements AddUserTransitions, State {

	public void start () {
		CreateUserController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected CreateUserController getController () {
		return new CreateUserController(CreateUserController.PASSWORD_FORMAT_NUMERIC);
	}
	
}
