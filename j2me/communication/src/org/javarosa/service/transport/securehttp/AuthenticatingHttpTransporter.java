/**
 * 
 */
package org.javarosa.service.transport.securehttp;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportMessageStatus;

import de.enough.polish.util.StreamUtil;

/**
 * An AuthenticatingHttpTransporter is a transporter which 
 * attempts to negotiate an HTTP request which may include
 * authentication challenges. The transporter will attempt
 * a request, possibly using cached credentials, will then
 * receive WWW-Authenticate challenges, issue them to the
 * message's authenticator, and retry the request if the 
 * challenge was handled.
 * 
 * @author ctsims
 *
 */
public class AuthenticatingHttpTransporter implements Transporter {
	
	AuthenticatedHttpTransportMessage message;

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.Transporter#getMessage()
	 */
	public TransportMessage getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.Transporter#setMessage(org.javarosa.services.transport.TransportMessage)
	 */
	public void setMessage(TransportMessage message) {
		if(! (message instanceof AuthenticatedHttpTransportMessage)) {
			throw new IllegalArgumentException("Cannot use Digest HTTP Transporter to send message, invalid message type.");
		}
		this.message = (AuthenticatedHttpTransportMessage)message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.Transporter#send()
	 */
	public TransportMessage send() {
		try {
			
			//Open the connection assuming either cached credentials
			//or no Authentication
			HttpConnection connection = getConnection();
			int response = connection.getResponseCode();
			
			if (response == HttpConnection.HTTP_UNAUTHORIZED) {
				
				String challenge = getChallenge(connection);
				//If authentication is needed, issue the challenge
				if (message.issueChallenge(connection, challenge)) {
					
					// The challenge was handled, and authentication
					// is now provided, try the request again after
					//closing the current connection.
					connection.close();
					connection = getConnection();
					
					//Handle the new response as-is, if authentication failed,
					//the sending process can issue a new request.
					return handleResponse(connection);
				} else {
					// The challenge couldn't be addressed. Set the message to
					// failure.
					return handleResponse(connection);
				}
			} else {
				//The message did not fail due to authorization problems, so
				//handle the response.
				return handleResponse(connection);
			}
		} catch (IOException e) {
			e.printStackTrace();
			message.setStatus(TransportMessageStatus.FAILED);
			message.setFailureReason("IO Exception");
			return message;
		}
	}
	
	private String getChallenge(HttpConnection connection ) throws IOException {
		//technically the standard
		
		String challenge = connection.getHeaderField("WWW-Authenticate");
		if(challenge == null) {
			//j2me sometimes lowercases everything;
			System.out.println("lowercase fallback!");
			challenge = connection.getHeaderField("www-authenticate");
		}
		return challenge;
	}

	private TransportMessage handleResponse(HttpConnection connection)
			throws IOException {
		int responseCode = connection.getResponseCode();
		
		if(responseCode >= 200 && responseCode < 300) {
			//It's all good, message was a success.
			message.setResponseCode(responseCode);
			message.setStatus(TransportMessageStatus.SENT);
			
			//Wire up the input stream from the connection to the message.
			message.setResponseStream(connection.openInputStream());
			return message;
		} else {
			message.setStatus(TransportMessageStatus.FAILED);
			message.setResponseCode(responseCode);
			
			//We'll assume that any failures come with a message which is sufficiently
			//small that they can be fit into memory.
			byte[] response = StreamUtil.readFully(connection.openInputStream());
			message.setFailureReason(new String(response));
			return message;
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private HttpConnection getConnection() throws IOException {
		if (this.message == null)
			throw new RuntimeException("Null message in getConnection()");

		HttpConnection conn = (HttpConnection) Connector.open(this.message.getUrl());
		if (conn == null)
			throw new RuntimeException("Null conn in getConnection()");
		if (this.message.getRequestProperties() == null) {
			throw new RuntimeException(
					"Null message.getRequestProperties() in getConnection()");
		}
		
		conn.setRequestMethod(message.getMethod());
		conn.setRequestProperty("User-Agent", this.message.getRequestProperties().getUserAgent());
		conn.setRequestProperty("Content-Language", this.message.getRequestProperties().getContentLanguage());
		conn.setRequestProperty("MIME-version", this.message.getRequestProperties().getMimeVersion());
		conn.setRequestProperty("Content-Type", this.message.getRequestProperties().getContentType());
		
		//Retrieve either the response auth header, or the cached guess
		String authorization = message.getAuthString();
		if(authorization != null) {
			conn.setRequestProperty("Authorization", authorization);
		}
		
		// any others
		Enumeration keys = this.message.getRequestProperties().getOtherProperties().keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) this.message.getRequestProperties()
					.getOtherProperties().get(key);
			conn.setRequestProperty(key, value);
		}

		return conn;
	}

}
