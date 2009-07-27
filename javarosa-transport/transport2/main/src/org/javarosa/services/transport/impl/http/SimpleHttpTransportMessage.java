package org.javarosa.services.transport.impl.http;

import java.io.IOException;
import java.io.InputStream;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;
import org.javarosa.services.transport.impl.StreamsUtil;

import de.enough.polish.io.Serializable;

public class SimpleHttpTransportMessage extends BasicTransportMessage implements
		Serializable {

	private String destinationURL;
	 
	public SimpleHttpTransportMessage(String str,String destinationURL) {
		setContent(str.getBytes());
		this.destinationURL = destinationURL;
	}
	public SimpleHttpTransportMessage(byte[] str,String destinationURL) {
		setContent(str);
		this.destinationURL = destinationURL;
	}
	public SimpleHttpTransportMessage(InputStream is,String destinationURL) throws IOException {
		setContent(StreamsUtil.readFromStream(is, -1));
		this.destinationURL = destinationURL;
	}

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
