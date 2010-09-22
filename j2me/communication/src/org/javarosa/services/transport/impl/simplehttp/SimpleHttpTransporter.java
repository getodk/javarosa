package org.javarosa.services.transport.impl.simplehttp;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.StreamsUtil;
import org.javarosa.services.transport.impl.TransportMessageStatus;

/**
 * The SimpleHttpTransporter is able to send SimpleHttpTransportMessages (text
 * over POST or GET)
 * 
 */
public class SimpleHttpTransporter implements Transporter {

	/**
	 * The message to be sent by this Transporter
	 */
	private SimpleHttpTransportMessage message;

	/**
	 * The HTTP method to be used by this Transporter POST by Default
	 */
	private String httpConnectionMethod = HttpConnection.POST;

	

	/**
	 * @param message
	 *            The message to be sent
	 */
	public SimpleHttpTransporter(SimpleHttpTransportMessage message) {
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.Transporter#getMessage()
	 */
	public TransportMessage getMessage() {
		return this.message;
	}

	public void setMessage(TransportMessage m) {

		this.message = (SimpleHttpTransportMessage) m;
		// if the message is set from a bulk sender
		// we are sharing a connection but each message
		// may have its own request properties
	}


	/*
	 *Set the HTTP Connection Method 
	 */
	public void setHttpConnectionMethod(String method)
	{
		if (method.equals(HttpConnection.GET))
		{
			this.httpConnectionMethod = HttpConnection.GET;
		}
		else if( method.equals(HttpConnection.POST)  )
		{
			this.httpConnectionMethod = HttpConnection.POST;
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.Transporter#send()
	 */
	public TransportMessage send() {
		HttpConnection conn = null;
		DataInputStream is = null;
		OutputStream os = null;
		try {

			System.out.println("Ready to send: " + this.message);

			conn = getConnection();

			System.out.println("Connection: " + conn);

			os = conn.openOutputStream();
			byte[] o = (byte[]) this.message.getContent();

			System.out.println("content: " + new String(o));
			StreamsUtil.writeToOutput(o, os);
			os.close();

			// Get the response
			is = (DataInputStream) conn.openDataInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamsUtil.writeFromInputToOutput(is, baos);
			is.close();
			int responseCode = conn.getResponseCode();
			System.out.println("response code: " + responseCode);
			// set return information in the message
			this.message.setResponseBody(baos.toByteArray());
			this.message.setResponseCode(responseCode);
			if (responseCode >= 200 && responseCode <= 299) {
				this.message.setStatus(TransportMessageStatus.SENT);
			}

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Connection failed: " + e.getClass() + " : "
					+ e.getMessage());
			this.message.setFailureReason(e.getMessage());
			this.message.incrementFailureCount();
		} finally {

			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if (conn != null)
				try {
					conn.close();
				} catch (IOException e) {
					// do nothing
				}
		}

		return this.message;

	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private HttpConnection getConnection() throws IOException {
		if (this.message == null)
			throw new RuntimeException("Null message in getConnection()");

		HttpConnection conn = (HttpConnection) Connector.open(this.message
				.getUrl());
		if (conn == null)
			throw new RuntimeException("Null conn in getConnection()");
		if (this.message.getRequestProperties() == null)
			throw new RuntimeException(
					"Null message.getRequestProperties() in getConnection()");
		if (this.message.getContent() == null)
			throw new RuntimeException(
					"Null message.getContent() in getConnection()");

		conn.setRequestMethod(this.httpConnectionMethod);
		conn.setRequestProperty("User-Agent", this.message
				.getRequestProperties().getUserAgent());
		conn.setRequestProperty("Content-Language", this.message
				.getRequestProperties().getContentLanguage());
		conn.setRequestProperty("MIME-version", this.message
				.getRequestProperties().getMimeVersion());
		conn.setRequestProperty("Content-Type", this.message
				.getRequestProperties().getContentType());

		conn.setRequestProperty("Content-Length", new Integer(
				((byte[]) this.message.getContent()).length).toString());
		// any others
		Enumeration keys = this.message.getRequestProperties()
				.getOtherProperties().keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) this.message.getRequestProperties()
					.getOtherProperties().get(key);
			conn.setRequestProperty(key, value);
		}

		return conn;

	}

}
