/**
 * 
 */
package org.javarosa.services.transport.impl.http;

import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.services.transport.api.ITransporter;
import org.javarosa.services.transport.api.TransportMessage;
import org.javarosa.services.transport.listeners.TransportListener;

/**
 * @author ctsims
 *
 */
public class HttpTransportMessage implements TransportMessage {
	
	private IDataPayload messagePayload;
	private Date created;
	private Date sent;
	
	private Vector transportListeners = new Vector();
	private int status = TransportListener.TRANSPORTING;
	
	private String URI;
	
	private int attempts = 1;
	
	private int delay = 0; 
	
	private byte[] response;
	
	private String contentType;
	
	private long nextReady;
	
	/**
	 * NOTE: For Serialization only!!!
	 */
	public HttpTransportMessage() {
		
	}
	public HttpTransportMessage(String URI, byte[] data) {
		this(URI, new ByteArrayPayload(data, null, ByteArrayPayload.PAYLOAD_TYPE_TEXT));
	}
	public HttpTransportMessage(String URI, IDataPayload payload) {
		HttpHeaderAppendingVisitor visitor = new HttpHeaderAppendingVisitor();
		messagePayload = (IDataPayload)payload.accept(visitor);
		contentType = visitor.getOverallContentType();
		nextReady = new Date().getTime();
	}

	public ITransporter createTransporter() {
		return new HttpTransporter().getTransporter();
	}

	public InputStream getContentStream() {
		return messagePayload.getPayloadStream();
	}

	public String getContentType() {
		return contentType;
	}

	public Date getCreated() {
		return created;
	}

	public Date getSent() {
		return sent;
	}

	public int getStatus() {
		return status;
	}
	
	public byte[] getResponse() {
		return response;
	}

	protected void setSuccesfulResponse(byte[] response) {
		this.response = response;
		this.sent = new Date();
		this.status = TransportListener.SUCCESS;
		this.succeedListeners(response);
	}
	
	public void setAttemptsLeft(int attempts) {
		this.attempts = attempts;
	}
	
	public int getAttemptsLeft() {
		return this.attempts;
	}
	
	public void setRetryDelay(int delay) {
		this.delay = delay;
	}
	
	public String getDestinationURL() {
		return URI;
	}
	
	public boolean isReady() {
		if(nextReady < new Date().getTime()) {
			return true;
		} else {
			return false;
		}
	}
	
	protected void fail(String reason, int code) { 
		fail(reason, code, false);
	}
	
	protected void fail(String reason, int code, boolean completely) {
		this.attempts--;
		this.nextReady = new Date().getTime() + delay;
		if(completely) {
			this.attempts = 0;
		}
		if(this.attempts > 0) {
			updateListeners("Sending failed, retrying " + getAttemptsLeft() + "more times...");
		}
		else {
			failListeners(code, reason);
		}
	}
	
	private void updateListeners(String message) {
		for(Enumeration en = transportListeners.elements(); en.hasMoreElements();) {
			TransportListener listener = (TransportListener)en.nextElement();
			listener.onUpdate(this, message);
		}
	}
	
	private void failListeners(int failureType, String message) {
		for(Enumeration en = transportListeners.elements(); en.hasMoreElements();) {
			TransportListener listener = (TransportListener)en.nextElement();
			listener.onFailure(this, failureType, message);
		}
	}
	
	private void succeedListeners(byte[] response) {
		for(Enumeration en = transportListeners.elements(); en.hasMoreElements();) {
			TransportListener listener = (TransportListener)en.nextElement();
			listener.onSuccess(this, response);
		}
	}

	public void addTransportListener(TransportListener listener) {
		transportListeners.addElement(listener);
	}
}
