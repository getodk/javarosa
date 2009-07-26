package org.javarosa.services.transport.impl;

import org.javarosa.services.transport.TransportMessage;

public abstract class BasicTransportMessage implements TransportMessage {

	private byte[] content;
	private String contentType;
	private boolean success;
	private int responseCode;
	private String failureReason;
	private int failureCount;
	private String queueIdentifier;

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

	public boolean isSuccess() {
		return this.success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
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
