package org.javarosa.services.transport;

import org.javarosa.services.transport.impl.http.HttpTransportResult;

public interface TransportService {

	/**
	 * 
	 * Send a String to a destination over Http
	 * 
	 * @param message The String to send
	 * @param url The destination
	 * @return An object providing information regarding success or failure
	 */
	public HttpTransportResult sendOverHttp(String message, String url);

}
