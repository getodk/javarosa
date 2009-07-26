package org.javarosa.services.transport.impl.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.StreamsUtil;

public class SimpleHttpTransporter implements Transporter {

	private HttpTransportMessage message;

	public SimpleHttpTransporter(HttpTransportMessage message) {
		this.message = message;
	}

	public TransportMessage getMessage() {
		return this.message;
	}

	public TransportMessage send() {

		try {
			HttpConnection_POST_Text conn = new HttpConnection_POST_Text(
					message.getDestinationURL());

			writeToConnection(conn, message.getContent());

			readResponse(conn, message);

			conn.close();
		} catch (ClassCastException e) {
			e.printStackTrace();
			message.setSuccess(false);
			message.setFailureReason(e.getMessage());
			message.incrementFailureCount();

		} catch (IOException e) {
			e.printStackTrace();
			message.setSuccess(false);
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
	private void writeToConnection(HttpConnection_POST_Text conn, byte[] bytes)
			throws IOException {
		OutputStream out = null;
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(bytes);
			out = conn.getConnection().openOutputStream();

			StreamsUtil.writeFromInputToOutput(in, out);
		} catch (IOException e) {
			throw e;

		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			if (in != null) {
				in.close();
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
	private HttpTransportMessage readResponse(HttpConnection_POST_Text conn,
			HttpTransportMessage message) throws IOException,
			ClassCastException {

		int responseCode = conn.getConnection().getResponseCode();
		if (responseCode == HttpConnection.HTTP_OK) {
			message.setSuccess(true);
			// TODO: what to do with the response body?
			readResponseBody(conn);
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
	private String readResponseBody(HttpConnection_POST_Text conn)
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
