package org.javarosa.services.transport.impl.http;

import org.javarosa.services.transport.impl.BasicTransportResult;

public class HttpTransportResult extends BasicTransportResult {

	private int httpResponseCode;

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	public void setHttpResponseCode(int httpResponseCode) {
		this.httpResponseCode = httpResponseCode;
	}

}
