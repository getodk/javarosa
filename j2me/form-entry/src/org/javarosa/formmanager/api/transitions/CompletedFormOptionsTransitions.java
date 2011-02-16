/**
 * 
 */
package org.javarosa.formmanager.api.transitions;

import org.javarosa.services.transport.TransportMessage;

/**
 * @author ctsims
 *
 */
public interface CompletedFormOptionsTransitions {
	public void sendData(TransportMessage message);
	public void skipSend(TransportMessage message);
}
