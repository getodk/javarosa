package org.javarosa.formmanager.view.transport;

import org.javarosa.services.transport.TransportMessage;

/**
 * An interface to bundle up application-specific code for dealing with responses from sending data
 * via the transport layer. For example, each application's server may respond with a different payload
 * when a message is successfully sent, and you want your app to show a customized message using the
 * information in this payload.
 * 
 * @author Drew Roos
 *
 */

public interface TransportResponseProcessor {

	/**
	 * Parse the response from the server after sending a message and construct output to
	 * present to the user
	 * 
	 * @param message
	 * @return
	 */
	String getResponseMessage (TransportMessage message);
	
}
