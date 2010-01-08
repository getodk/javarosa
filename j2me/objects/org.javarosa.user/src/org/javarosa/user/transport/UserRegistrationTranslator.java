/**
 * 
 */
package org.javarosa.user.transport;

import java.io.DataInputStream;

import org.javarosa.services.transport.TransportMessage;

/**
 * @author ctsims
 *
 */
public interface UserRegistrationTranslator<M extends TransportMessage> {
	
	public M getUserRegistrationMessage();
	
	public boolean readResponse(M message);
}
