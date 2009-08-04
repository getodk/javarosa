package org.javarosa.services.transport.impl.simplehttp;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.HttpConnection;

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
	 * Constants from HttpConnection class:
	 * 
	 * HttpConnection.POST
	 * HttpConnection.GET
	 */
	private String requestType;

	 
	public SimpleHttpTransportMessage( String str, String destinationURL) {
		setContent(str.getBytes());
		this.destinationURL = destinationURL;
		this.requestType=HttpConnection.POST;
	}
	
	public SimpleHttpTransportMessage(String requestType,String str, String destinationURL) {
		setContent(str.getBytes());
		this.destinationURL = destinationURL;
		this.requestType=requestType;
	}

	 
	public SimpleHttpTransportMessage(String requestType,byte[] str, String destinationURL) {
		setContent(str);
		this.destinationURL = destinationURL;
		this.requestType=requestType;
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

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
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
