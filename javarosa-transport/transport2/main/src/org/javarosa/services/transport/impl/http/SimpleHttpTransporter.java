package org.javarosa.services.transport.impl.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.MessageStatus;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.StreamsUtil;

/**
 * The SimpleHttpTransporter is able to send SimpleHttpTransportMessages
 * 
 */
public class SimpleHttpTransporter implements Transporter {

	private SimpleHttpTransportMessage message;

	public SimpleHttpTransporter(SimpleHttpTransportMessage message) {
		this.message = message;
	}

	public TransportMessage getMessage() {
		return this.message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.Transporter#send()
	 */
	public TransportMessage send() {

		try {
			SimpleHttpConnection conn = new SimpleHttpConnection(message
					.getDestinationURL());

			writeToConnection(conn, message.getContent());

			readResponse(conn, message);
			// getting here doesn't necessarily indicate successful send
			// (depends on HTTP response code)
			conn.close();
		} catch (IOException e) {
			e.printStackTrace();
			message.setFailureReason(e.getMessage());
			message.incrementFailureCount();
		}

		return message;

	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * @param conn
	 * @param bytes
	 * @throws IOException
	 */
	private void writeToConnection(SimpleHttpConnection conn, byte[] bytes)
			throws IOException {
		OutputStream out = null;
		try {
			// earlier code was commented: Problem exists here on 3110c CommCare
			// Application: open hangs
			out = conn.getConnection().openOutputStream();
			System.out.println("writing: " + new String(bytes));
			StreamsUtil.writeToOutput(bytes, out);
		} catch (IOException e) {
			throw e;

		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}

		}

	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * @param conn
	 * @param result
	 * @return
	 * @throws IOException
	 * @throws ClassCastException
	 */
	private SimpleHttpTransportMessage readResponse(SimpleHttpConnection conn,
			SimpleHttpTransportMessage message) throws IOException,
			ClassCastException {

		int responseCode = conn.getConnection().getResponseCode();
		if (responseCode == HttpConnection.HTTP_OK) {
			// TODO: what to do with the response body?
			readResponseBody(conn);
			message.setStatus(MessageStatus.SENT);
		} else {
			message.setResponseCode(responseCode);
		}

		return message;

	}

	/**
	 * 
	 * 
	 * 
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	private String readResponseBody(SimpleHttpConnection conn)
			throws IOException {
		InputStream in = null;
		String r = null;
		try {
			in = conn.getConnection().openInputStream();
			int len = (int) conn.getConnection().getLength();
			byte[] response = StreamsUtil.readFromStream(in, len);
			r = new String(response);
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return r;
	}

}
