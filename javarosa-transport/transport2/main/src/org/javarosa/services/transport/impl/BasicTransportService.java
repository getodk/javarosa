package org.javarosa.services.transport.impl;

import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.http.HttpTransport;
import org.javarosa.services.transport.impl.http.HttpTransportResult;

public class BasicTransportService implements TransportService {

	public HttpTransportResult sendOverHttp(String message, String url) {
		HttpTransport http = new HttpTransport(url);
		return http.send(message);
	}

}
