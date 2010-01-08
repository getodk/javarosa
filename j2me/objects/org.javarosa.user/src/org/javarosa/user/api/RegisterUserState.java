/**
 * 
 */
package org.javarosa.user.api;

import java.io.InputStream;

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
	
	public RegisterUserState(User u) {
		this.user = user;
	}
	
	public void start () {
		RegisterUserController<SimpleHttpTransportMessage> controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected RegisterUserController<SimpleHttpTransportMessage> getController () {
		throw new RuntimeException();
		//return new RegisterUserController<SimpleHttpTransportMessage>(new HttpUserRegistrationTranslator(user), this);
	}

	public abstract SimpleHttpTransportMessage buildHttpMesage(InputStream payloadStream);
	
	
}
