/**
 * 
 */
package org.javarosa.user.api;

import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.user.api.transitions.RegisterUserTransitions;
import org.javarosa.user.model.User;
import org.javarosa.user.transport.HttpUserRegistrationTranslator;

/**
 * @author ctsims
 *
 */
public abstract class RegisterUserState implements RegisterUserTransitions {
	
	User user;
	
	public RegisterUserState(User user) {
		this.user = user;
	}
	
	public void start () {
		RegisterUserController<SimpleHttpTransportMessage> controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected RegisterUserController<SimpleHttpTransportMessage> getController () {
		return new RegisterUserController<SimpleHttpTransportMessage>(new HttpUserRegistrationTranslator(user,getRegistrationURL()));
	}

	public abstract String getRegistrationURL();
	
	
}
