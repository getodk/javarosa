package org.javarosa.services.transport.impl.simplehttp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.StreamsUtil;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.simplehttp.httpwrapper.HttpConnectionWrapper;
import org.javarosa.services.transport.impl.simplehttp.httpwrapper.Level;

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
		byte[] o = (byte[]) this.message.getContent();

		String content = new String(o);
		System.out.println("Getting ready to send: " + content);
		DataInputStream is = null;
		DataOutputStream os = null;
		try {
			HttpConnectionWrapper.logger.setLevel(Level.FINE);
			conn = new HttpConnectionWrapper(getConnection(message
					.getDestinationURL()));
			conn.setRequestProperty("Content-Length", new Integer(content
					.length()).toString());

			//this.message.setResponseCode(conn.getResponseCode());
			//if (conn.getResponseCode() == HttpConnection.HTTP_OK) {
				os = (DataOutputStream) conn.openDataOutputStream();

				os.writeUTF(content);
			//	os.flush();

				// Get the response
				is = (DataInputStream) conn.openDataInputStream();
				// is = c.openInputStream();
				int ch;
				StringBuffer sb = new StringBuffer();
				while ((ch = is.read()) != -1) {
					sb.append((char) ch);
				}
				System.out.println("messge: " + sb.toString());
				message.setResponseBody(sb.toString());
				is.close();
				this.message.setResponseCode(conn.getResponseCode());
			//}

			conn.close();
		} catch (Exception e) {
			System.out.println("Connection failed: " + e.getClass() + " : "
					+ e.getMessage());
			message.setFailureReason(e.getMessage());
			message.incrementFailureCount();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if (os != null) {
				try {
					os.close();
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

		return message;

	}

	/**
	 * 
	 * 
	 * Write the byte array to the HttpConnection
	 * 
	 * @param conn
	 * @param bytes
	 * @throws IOException
	 */
	private void writeToConnection(HttpConnection conn, InputStream is)
			throws Exception {
		OutputStream out = null;
		try {

			out = conn.openOutputStream();

			StreamsUtil.writeFromInputToOutput(is, out);

		} catch (Exception e) {
			e.printStackTrace();
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
	 * Read the response from the HttpConnection and record in the
	 * SimpleHttpTransportMessage
	 * 
	 * 
	 * @param conn
	 * @param result
	 * @return
	 * @throws IOException
	 * @throws ClassCastException
	 */
	private SimpleHttpTransportMessage readResponse(HttpConnection conn,
			SimpleHttpTransportMessage message) throws IOException {

		int responseCode = conn.getResponseCode();
		if (responseCode == HttpConnection.HTTP_OK) {
			message.setResponseBody(readResponseBody(conn));
			message.setStatus(TransportMessageStatus.SENT);
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
	private String readResponseBody(HttpConnection conn) throws IOException {
		InputStream in = null;
		String r = null;
		try {
			in = conn.openInputStream();
			int len = (int) conn.getLength();
			byte[] response = StreamsUtil.readFromStream(in, len);
			r = new String(response);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return r;
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private HttpConnection getConnection(String url) throws IOException {
		HttpConnection conn = (HttpConnection) Connector.open(url);
		conn.setRequestMethod(HttpConnection.POST);
		conn.setRequestProperty("User-Agent",
				"Profile/MIDP-2.0 Configuration/CLDC-1.1");
		conn.setRequestProperty("Content-Language", "en-US");
		conn.setRequestProperty("MIME-version", "1.0");
		conn.setRequestProperty("Content-Type", "text/plain");
		return conn;

	}

}
