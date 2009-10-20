package org.javarosa.services.transport.impl.binarysms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

/**
 * SMS message object
 * 
 * Since the message to be sent may require to be partitioned into more than one
 * SMS payloads, the content of the SMSTransportMessage is a Vector of Strings
 * (in the simplest case, vector size = 1)
 * 
 * 
 */
public class BinarySMSTransportMessage extends BasicTransportMessage implements
		Serializable {

	/**
	 * 
	 */
	private String destinationURL;

	/**
	 * @param str
	 * @param destinationURL
	 */
	public BinarySMSTransportMessage(byte[] bytes, String destinationURL) {
		this.destinationURL = destinationURL;
		setContent(bytes);
	}

	public boolean isCacheable() {
		return true;
	}

	/**
	 * @return
	 */
	public String getDestinationURL() {
		return destinationURL;
	}

	/**
	 * @param destinationURL
	 */
	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new BinarySMSTransporter(this);
	}

	public InputStream getContentStream() {
		return new ByteArrayInputStream((byte[]) getContent());
	}
}
