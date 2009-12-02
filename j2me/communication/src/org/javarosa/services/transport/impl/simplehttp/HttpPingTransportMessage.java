package org.javarosa.services.transport.impl.simplehttp;

/**
 
 * 
 */
public class HttpPingTransportMessage extends SimpleHttpTransportMessage {
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
