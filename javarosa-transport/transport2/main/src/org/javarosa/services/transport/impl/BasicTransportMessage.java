package org.javarosa.services.transport.impl;

import java.util.Date;

import org.javarosa.services.transport.MessageStatus;
import org.javarosa.services.transport.TransportMessage;

public abstract class BasicTransportMessage implements TransportMessage {

	private byte[] content;
	private String contentType;
	private int status;
	private int responseCode;
	private String failureReason;
	private int failureCount;
	private String queueIdentifier;
	private Date created;
	private long queuingDeadline;
	
	
	
	public long getQueuingDeadline() {
		return queuingDeadline;
	}

	public void setQueuingDeadline(long queuingDeadline) {
		this.queuingDeadline = queuingDeadline;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getResponseCode() {
		return this.responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public boolean isSuccess(){
		return this.status==MessageStatus.SENT;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void incrementFailureCount() {
		this.failureCount++;
	}

	public String getQueueIdentifier() {
		return queueIdentifier;
	}

	public void setQueueIdentifier(String queueIdentifier) {
		this.queueIdentifier = queueIdentifier;
	}

}
