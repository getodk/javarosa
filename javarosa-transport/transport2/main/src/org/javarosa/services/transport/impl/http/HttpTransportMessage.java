package org.javarosa.services.transport.impl.http;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

public class HttpTransportMessage extends BasicTransportMessage implements
		Serializable {

	private String destinationURL;
	
	public int getTransportMethod() {
		return TransportMessage.TRANSPORT_METHOD_HTTP;
	}

	public String getDestinationURL() {
		return destinationURL;
	}

	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}

	

}
