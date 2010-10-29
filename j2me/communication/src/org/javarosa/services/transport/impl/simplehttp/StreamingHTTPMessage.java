package org.javarosa.services.transport.impl.simplehttp;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

public abstract class StreamingHTTPMessage extends SimpleHttpTransportMessage {

	public StreamingHTTPMessage (String url) {
		super((byte[])null, url);
		this.setCacheable(false);
	}

	public abstract void writeBody(OutputStream os) throws IOException;
	
	public void setCacheable(boolean cacheable) {
		if (cacheable) {
			throw new RuntimeException("streaming messages cannot be cached!");
		}
	}
	
	public void setHttpConnectionMethod(String method) {
		if (HttpConnection.GET.equals(method)) {
			throw new RuntimeException("streaming messages must use method POST or PUT");
		}
	}
	
	public byte[] getContent() {
		throw new RuntimeException("streaming messages have no static content!");
	}

	public int getContentLength() {
		return -1;
	}
	
	public String toString() {
		String s = "#" + getCacheIdentifier() + " (http-stream)";
		if (getResponseCode() > 0)
			s += " " + getResponseCode();
		return s;
	}
}
