package org.javarosa.services.transport.impl.simplehttp;

import java.io.ByteArrayInputStream;
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
	private String url;

	/**
	 * Http response code
	 */
	private int responseCode;

	/**
	 * 
	 */
	private String responseBody;

	/**
	 * 
	 */
	private HttpRequestProperties requestProperties = new HttpRequestProperties();

	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(String str, String url) {
		setContent(str.getBytes());
		this.url = url;
	}

	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(byte[] str, String url) {
		setContent(str);
		this.url = url;
	}

	/**
	 * @param is
	 * @param destinationURL
	 * @throws IOException
	 */
	public SimpleHttpTransportMessage(InputStream is, String url)
			throws IOException {

		setContent(StreamsUtil.readFromStream(is, -1));
		this.url = url;
	}

	public HttpRequestProperties getRequestProperties() {
		return requestProperties;
	}

	public void setRequestProperties(HttpRequestProperties requestProperties) {
		this.requestProperties = requestProperties;
	}

	public boolean isCacheable() {
		return true;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#getTransporter()
	 */
	public Transporter createTransporter() {
		return new SimpleHttpTransporter(this);
	}

	public String toString() {
		String s = "#" + getCacheIdentifier() + " (http)";
		if (getResponseCode() > 0)
			s += " " + getResponseCode();
		return s;
	}

	public InputStream getContentStream() {
		return new ByteArrayInputStream((byte[]) getContent());
	}

}
