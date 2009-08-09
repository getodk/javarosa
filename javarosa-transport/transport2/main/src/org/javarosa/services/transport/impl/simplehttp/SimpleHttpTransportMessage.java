package org.javarosa.services.transport.impl.simplehttp;

import java.io.IOException;
import java.io.InputStream;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;
import org.javarosa.services.transport.impl.StreamsUtil;

import de.enough.polish.io.Serializable;

/**
 * A message which implements the simplest Http transfer - plain text via POST
 * request
 * 
 */
public class SimpleHttpTransportMessage extends BasicTransportMessage implements
		Serializable {

	/**
	 * An http url, to which the message will be POSTed
	 */
	private String destinationURL;

	/**
	 * Http response code
	 */
	private int responseCode;

	/**
	 * 
	 */
	private String responseBody;

	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(String str, String destinationURL) {
		setContent(str.getBytes());
		this.destinationURL = destinationURL;
	}

	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(byte[] str, String destinationURL) {
		setContent(str);
		this.destinationURL = destinationURL;
	}

	/**
	 * @param is
	 * @param destinationURL
	 * @throws IOException
	 */
	public SimpleHttpTransportMessage(InputStream is, String destinationURL)
			throws IOException {
		setContent(StreamsUtil.readFromStream(is, -1));
		this.destinationURL = destinationURL;
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

	/**
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * @param responseBody
	 */
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#getTransporter()
	 */
	public Transporter createTransporter() {
		return new SimpleHttpTransporter(this);
	}

}
