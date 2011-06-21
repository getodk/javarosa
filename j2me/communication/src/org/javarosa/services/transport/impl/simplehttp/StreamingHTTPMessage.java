package org.javarosa.services.transport.impl.simplehttp;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import org.javarosa.core.services.Logger;
import org.javarosa.services.transport.TransportService;

public abstract class StreamingHTTPMessage extends SimpleHttpTransportMessage {

	public StreamingHTTPMessage (String url) {
		super((byte[])null, url);
		this.setCacheable(false);
	}

	public final void writeBody(OutputStream os) throws IOException {
		OutputStreamC osc = new OutputStreamC(os);
		boolean ioex = false;
		try {
			_writeBody(osc);
		} catch (IOException e) {
			ioex = true;
			throw e;
		} finally {
			if (ioex || osc.count > TransportService.PAYLOAD_SIZE_REPORTING_THRESHOLD) {
				long count = osc.count;
				long countAtt = osc.countAttempt;
				Logger.log("send", "stream sent " + count + (countAtt != count ? ".." + countAtt : ""));
			}
		}
	}
	
	public abstract void _writeBody(OutputStream os) throws IOException;
	
	public void setCacheable(boolean cacheable) {
		if (cacheable) {
			throw new RuntimeException("streaming messages cannot be cached!");
		}
		
		super.setCacheable(cacheable);
	}
	
	public void setHttpConnectionMethod(String method) {
		if (HttpConnection.GET.equals(method)) {
			throw new RuntimeException("streaming messages must use method POST or PUT");
		}
		
		super.setHttpConnectionMethod(method);
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
	
	protected class OutputStreamC extends OutputStream {
		public long count;
		public long countAttempt;
		OutputStream os;
		
		public OutputStreamC (OutputStream os) {
			this.os = os;
			this.count = 0;
			this.countAttempt = 0;
		}
		
		public void write(byte[] b) throws IOException {
			countAttempt += b.length;
			os.write(b);
			count = countAttempt;
		}
		
		public void write(byte[] b, int off, int len) throws IOException {
			countAttempt += len;
			os.write(b, off, len);
			count = countAttempt;
		}
		
		public void write(int b) throws IOException {
			countAttempt += 1;
			os.write(b);
			count = countAttempt;
		}
		
		public void close() throws IOException {
			os.close();
		}
		
		public void flush() throws IOException {
			os.flush();
		}
	}
}
