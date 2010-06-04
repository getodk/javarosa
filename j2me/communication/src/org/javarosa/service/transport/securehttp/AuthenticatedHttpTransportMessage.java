/**
 * 
 */
package org.javarosa.service.transport.securehttp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.microedition.io.HttpConnection;

import org.javarosa.core.services.transport.payload.IDataPayload;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.simplehttp.HttpRequestProperties;

/**
 * An AuthenticatedHttpTransportMessage is a transport message which is used to
 * either perform a GET or POST request to an HTTP server, which includes the 
 * capacity for authenticating with that server if a WWW-Authenticate challenge
 * is issued. 
 * 
 * AuthenticatedHttpTransportMessage are currently unable to cache themselves
 * natively with the transport service.
 * 
 * @author ctsims
 *
 */
public class AuthenticatedHttpTransportMessage implements TransportMessage {
	Date created;
	Date sent;
	String URL;
	int recordId;
	int status;
	
	int responseCode;
	InputStream response;
	HttpAuthenticator authenticator;
	String authentication;
	String failureReason;
	
	IDataPayload payload;
	
	
	/**
	 * Creates a message which will perform an HTTP GET Request to the server referenced at
	 * the given URL. 
	 * 
	 * @param URL The requested server URL
	 * @param authenticator An authenticator which is capable of providing credentials upon
	 * request.
	 * @return A new authenticated HTTP message ready for sending.
	 */
	public static AuthenticatedHttpTransportMessage AuthenticatedHttpRequest(String URL, HttpAuthenticator authenticator) {
		AuthenticatedHttpTransportMessage message = new AuthenticatedHttpTransportMessage(URL, authenticator);
		return message;	
	}
	
	/**
	 * Creates a message which will perform an HTTP POST Request to the server referenced at
	 * the given URL. 
	 * 
	 * @param URL The requested server URL
	 * @param authenticator An authenticator which is capable of providing credentials upon
	 * request.
	 * @param payload A data payload which will be posted to the remote server.
	 * @return A new authenticated HTTP message ready for sending.
	 */
	public static AuthenticatedHttpTransportMessage AuthenticatedHttpPOST(String URL, IDataPayload payload, HttpAuthenticator authenticator) {
		AuthenticatedHttpTransportMessage message = new AuthenticatedHttpTransportMessage(URL, authenticator);
		message.payload = payload;
		return message;
	}

	private AuthenticatedHttpTransportMessage(String URL, HttpAuthenticator authenticator) {
		created = new Date();
		this.status = TransportMessageStatus.QUEUED;
		this.URL = URL;
		this.authenticator = authenticator;
	}
	
	/**
	 * @return The HTTP request method (Either GET or POST) for
	 * this message.
	 */
	public String getMethod() {
		if(payload == null) {
			return HttpConnection.GET;
		}
		return HttpConnection.POST;
	}
	
	/**
	 * @return The HTTP URL of the server for this message
	 */
	public String getUrl() {
		return URL;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new AuthenticatingHttpTransporter();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getCacheIdentifier()
	 */
	public String getCacheIdentifier() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getContent()
	 */
	public Object getContent() {
		throw new RuntimeException("The content of a Digest HTTP message can only be viewed as a stream"); 
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getContentStream()
	 */
	public InputStream getContentStream() {
		if(payload == null) {
			return new ByteArrayInputStream("".getBytes());
		}
		return payload.getPayloadStream();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getCreated()
	 */
	public Date getCreated() {
		return created;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getFailureCount()
	 */
	public int getFailureCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getFailureReason()
	 */
	public String getFailureReason() {
		return failureReason;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getQueuingDeadline()
	 */
	public long getQueuingDeadline() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getSent()
	 */
	public Date getSent() {
		return sent;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#getStatus()
	 */
	public int getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#isCacheable()
	 */
	public boolean isCacheable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#isSuccess()
	 */
	public boolean isSuccess() {
		return this.status == TransportMessageStatus.SENT;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#setCacheIdentifier(java.lang.String)
	 */
	public void setCacheIdentifier(String id) {
		//nothing, no caching.
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#setFailureReason(java.lang.String)
	 */
	public void setFailureReason(String reason) {
		failureReason = reason;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#setSendingThreadDeadline(long)
	 */
	public void setSendingThreadDeadline(long time) {
		//Not valid
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#setStatus(int)
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.Persistable#getID()
	 */
	public int getID() {
		return recordId;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.Persistable#setID(int)
	 */
	public void setID(int ID) {
		this.recordId = ID;
	}

	/**
	 * Issues an authentication challenge from the provided HttpConnection
	 * 
	 * @param connection The connection which issued the challenge
	 * @param challenge The WWW-Authenticate challenge issued.
	 * @return True if the challenge was addressed by the message's authenticator,
	 * and the request should be retried, False if the challenge could not be 
	 * addressed.
	 */
	public boolean issueChallenge(HttpConnection connection, String challenge) {
		authentication = this.authenticator.challenge(connection, challenge, this);
		if(authentication == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * @return the current best-guess authorization header for this message, 
	 * either produced as a response to a WWW-Authenticate challenge, or 
	 * provided by the authentication cache based on previous requests
	 * (if enabled and relevant in the message's authenticator). 
	 */
	public String getAuthString() {
		if(authentication == null) {
			//generally pre-challenge
			return authenticator.checkCache(this);
		}
		return authentication;
	}
	
	/**
	 * @param code The response code of the most recently attempted
	 * request.
	 */
	public void setResponseCode(int code) {
		this.responseCode = code;
	}
	
	/**
	 * @return code The response code of the most recently attempted
	 * request.
	 */
	public int getResponseCode() {
		return responseCode;
	}
	
	/**
	 * Sets the stream of the response from a delivery attempt
	 * @param response The stream provided from the http connection
	 * from a deliver attempt
	 */
	protected void setResponseStream(InputStream response) {
		this.response = response;
	}
	
	/**
	 * @return The stream provided from the http connection
	 * from the previous deliver attempt
	 */
	public InputStream getResponse() {
		return response;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		//doesn't cache;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		//doesn't cache;
	}

	/**
	 * @return The properties for this http request (other than
	 * authorization headers).
	 */
	public HttpRequestProperties getRequestProperties() {
		return new HttpRequestProperties();
	}

}
