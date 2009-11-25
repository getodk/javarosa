package org.javarosa.services.transport.http;

import org.javarosa.services.transport.TransportResult;

public class HttpTransportResult implements TransportResult {
	
	private byte[] payload;
	private boolean success;
	private int responseCode;
	
	
	 
 
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
	 
	
	

}
