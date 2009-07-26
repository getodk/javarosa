package org.javarosa.services.transport.impl;

import org.javarosa.services.transport.TransportResult;

public class BasicTransportResult implements TransportResult {

	private byte[] payload;
	private boolean success;
	private int responseCode;
	private String failureReason;
	private int failureCount;

	public int getResponseCode() {
		return this.responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
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

	
	
}
