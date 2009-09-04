package org.javarosa.services.transport.impl.simplehttp;

import de.enough.polish.io.Serializable;

/**
 
 * 
 */
public class HttpPingTransportMessage extends SimpleHttpTransportMessage
		implements Serializable {

	/**
	 * @param str
	 * @param destinationURL
	 */
	public HttpPingTransportMessage(String str, String destinationURL) {
		super(str, destinationURL);
	}

	public boolean isCacheable() {
		return false;
	}

}
