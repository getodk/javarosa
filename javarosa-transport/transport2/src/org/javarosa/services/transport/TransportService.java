package org.javarosa.services.transport;

import org.javarosa.services.transport.http.HttpTransport;

public class TransportService  {

	public static TransportResult sendViaHttp(String message,String url) {
		HttpTransport http = new HttpTransport(url);
		return http.send(message);
	}

}
