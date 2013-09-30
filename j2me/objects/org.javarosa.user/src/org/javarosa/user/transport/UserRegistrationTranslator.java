/**
 * 
 */
package org.javarosa.user.transport;

import java.io.IOException;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.UnrecognizedResponseException;
import org.javarosa.user.model.User;

/**
 * @author ctsims
 *
 */
public interface UserRegistrationTranslator<M extends TransportMessage> {
	
	public M getUserRegistrationMessage() throws IOException;
	
	public User readResponse(M message) throws UnrecognizedResponseException;
	
	public String getResponseMessageString();
}
