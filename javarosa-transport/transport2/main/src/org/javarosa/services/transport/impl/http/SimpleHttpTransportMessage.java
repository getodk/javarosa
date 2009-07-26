package org.javarosa.services.transport.impl.http;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

public class SimpleHttpTransportMessage extends BasicTransportMessage implements
		Serializable {

	private String destinationURL;

	 
	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getTransporter()
	 */
	public Transporter getTransporter() {
		return new SimpleHttpTransporter(this);
	}
	 
	public String getDestinationURL() {
		return destinationURL;
	}

	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}

	

}
