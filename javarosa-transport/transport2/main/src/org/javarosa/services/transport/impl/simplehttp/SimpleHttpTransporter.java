package org.javarosa.services.transport.impl.simplehttp;

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
 * over POST)
 * 
 */
public class SimpleHttpTransporter implements Transporter {

	/**
	 * The message to be sent by this Transporter
	 */
	private SimpleHttpTransportMessage message;

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

			conn = getConnection();

			os = conn.openOutputStream();
			byte[] o = (byte[]) this.message.getContent();
			StreamsUtil.writeToOutput(o, os);
			os.close();

			// Get the response
			is = (DataInputStream) conn.openDataInputStream();
			int ch;
			StringBuffer sb = new StringBuffer();
			while ((ch = is.read()) != -1) {
				sb.append((char) ch);
			}
			is.close();
			int responseCode = conn.getResponseCode();

			// set return information in the message
			this.message.setResponseBody(sb.toString());
			this.message.setResponseCode(responseCode);
			if (responseCode == HttpConnection.HTTP_OK) {
				this.message.setStatus(TransportMessageStatus.SENT);
			}

			conn.close();
		} catch (Exception e) {
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
		HttpConnection conn = (HttpConnection) Connector.open(this.message
				.getUrl());
		conn.setRequestMethod(HttpConnection.POST);
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
