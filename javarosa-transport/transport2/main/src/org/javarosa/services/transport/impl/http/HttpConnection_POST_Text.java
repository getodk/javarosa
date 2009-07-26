package org.javarosa.services.transport.impl.http;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class HttpConnection_POST_Text {

	private HttpConnection connection;

	/**
	 * 
	 * <a href=http://java.sun.com/javame/reference/apis/jsr118/javax/microedition/io/HttpConnection.html>HttpConnection</a>
	 * 
	 * @param url
	 * @throws IOException
	 *             , ClassCastException
	 */
	public HttpConnection_POST_Text(String url) throws IOException,
			ClassCastException {
		super();
		this.connection = (HttpConnection) Connector.open(url);

		this.connection.setRequestMethod(HttpConnection.POST);
		this.connection.setRequestProperty("User-Agent",
				"Profile/MIDP-2.0 Configuration/CLDC-1.1");
		this.connection.setRequestProperty("Content-Language", "en-US");
		this.connection.setRequestProperty("MIME-version", "1.0");
		this.connection.setRequestProperty("Content-Type", "text/plain");

	}

	public HttpConnection getConnection() {
		return this.connection;
	}

	public void close() throws IOException {
		this.connection.close();
	}

}
