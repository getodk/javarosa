package org.javarosa.services.transport.impl.simplehttp;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.core.log.WrappedException;
import org.javarosa.core.services.Logger;
import org.javarosa.core.util.StreamsUtil;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.BasicTransportMessage;
import org.javarosa.services.transport.impl.TransportMessageStatus;

/**
 * A message which implements the simplest Http transfer - plain text via POST
 * request
 * 
 */
public class SimpleHttpTransportMessage extends BasicTransportMessage {

	
	private byte[] content;
	/**
	 * An http url, to which the message will be POSTed
	 */
	private String url;

	/**
	 * Http response code
	 */
	private int responseCode;

	private byte[] responseBody;
	
	private boolean cacheable = true;
	
	/**
	 * Http connection method.
	 */

	private String httpConnectionMethod = HttpConnection.POST;


	public SimpleHttpTransportMessage() {
		//ONLY FOR SERIALIZATION
	}
	
	public SimpleHttpTransportMessage(String url) {
		this.url = url;
		this.setHttpConnectionMethod(HttpConnection.GET);
	}
	
	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(String str, String url) {
		content = str.getBytes();
		this.url = url;
	}

	/**
	 * @param str
	 * @param destinationURL
	 */
	public SimpleHttpTransportMessage(byte[] str, String url) {
		content = str;
		this.url = url;
	}

	/**
	 * @param is
	 * @param destinationURL
	 * @throws IOException
	 */
	public SimpleHttpTransportMessage(InputStream is, String url) throws IOException {
		content = StreamsUtil.readFromStream(is, -1);
		this.url = url;
	}

	public HttpRequestProperties getRequestProperties() {
		return new HttpRequestProperties();
	}

	public boolean isCacheable() {
		return cacheable;
	}
	

	public byte[] getContent() {
		return content;
	}
	
	public int getContentLength() {
		if(this.getContent() != null) {
			return getContent().length;
		} else {
			return -1;
		}
	}
	
	/**
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return
	 */
	public byte[] getResponseBody() {
		return responseBody;
	}

	/**
	 * @param responseBody
	 */
	public void setResponseBody(byte[] responseBody) {
		this.responseBody = responseBody;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}
	
	public String getConnectionMethod() {
		return this.httpConnectionMethod;
	}
	
	public void setHttpConnectionMethod(String httpConnectionMethod)
	{
		this.httpConnectionMethod = httpConnectionMethod;
		
		//There's no meaning to caching a GET
		if(this.httpConnectionMethod != HttpConnection.POST) {
			setCacheable(false);
		}
	}

	
	
	
	
	
	public void send() {
		HttpConnection conn = null;
		DataInputStream is = null;
		OutputStream os = null;
		
		long responseLength = -1;
		long[] responseRead = {0};
		boolean ex = false;
		
		try {
			System.out.println("Ready to send: " + this);
			conn = getConnection(this.getConnectionMethod());
			System.out.println("Connection: " + conn);

			os = conn.openOutputStream();
			writeBody(os);
			os.close();

			// Get the response
			int responseCode = conn.getResponseCode();
			System.out.println("response code: " + responseCode);

			responseLength = conn.getLength();
			
			is = (DataInputStream) conn.openDataInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamsUtil.writeFromInputToOutput(is, baos, responseRead);

			// set return information in the message
			this.setResponseBody(baos.toByteArray());
			this.setResponseCode(responseCode);
			if (responseCode >= 200 && responseCode <= 299) {
				this.setStatus(TransportMessageStatus.SENT);
			} else {
				Logger.log("send", this.getTag() + " http resp code: " + responseCode);
			}

			conn.close();
		} catch (Exception e) {
			ex = true;
			e.printStackTrace();
			System.out.println("Connection failed: " + e.getClass() + " : "
					+ e.getMessage());
			this.setFailureReason(WrappedException.printException(e));
			this.incrementFailureCount();
		} finally {
			logRecv(responseLength, responseRead[0], ex);
						
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}

	protected void writeBody(OutputStream os) throws IOException {
		byte[] o = this.getContent();
		if (o != null) {
			if (o.length > TransportService.PAYLOAD_SIZE_REPORTING_THRESHOLD) {
				Logger.log("send", "size " + o.length);
			}
			System.out.println("content: " + new String(o));
			
			long[] tally = {0};
			try {
				StreamsUtil.writeToOutput(o, os, tally);
			} finally {
				if (tally[0] != o.length) {
					Logger.log("send", "only " + tally[0] + " of " + o.length);
				}
			}
		} else {
			System.out.println("no request body");
		}
	}
	
	public static void logRecv (long total, long read, boolean ex) {
		try {
			boolean hasLength = (total >= 0);	//whether we have total length
			boolean diff;						//whether bytes read differed from total length
			boolean logIt;						//whether to log stats
			
			if (hasLength) {
				diff = (total != read);
				logIt = (total > TransportService.PAYLOAD_SIZE_REPORTING_THRESHOLD || diff);
			} else {
				logIt = (read > TransportService.PAYLOAD_SIZE_REPORTING_THRESHOLD || ex);
				diff = false;
			}
	
			if (logIt) {
				Logger.log("recv", read + (diff ? " of " + total : ""));
			}
		} catch (Exception e) {
			//safety first!
			Logger.exception("TransportMessage.logRecv", e);
		}
	}
	
	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private HttpConnection getConnection(String connectionMethod) throws IOException {
		HttpConnection conn = (HttpConnection) Connector.open(this.getUrl());
		if (conn == null)
			throw new RuntimeException("Null conn in getConnection()");
		
		HttpRequestProperties requestProps = this.getRequestProperties();
		if (requestProps == null)
			throw new RuntimeException("Null message.getRequestProperties() in getConnection()");

		conn.setRequestMethod(connectionMethod);
		conn.setRequestProperty("User-Agent", requestProps.getUserAgent());
		conn.setRequestProperty("Content-Language", requestProps.getContentLanguage());
		conn.setRequestProperty("MIME-version", requestProps.getMimeVersion());
		conn.setRequestProperty("Content-Type", requestProps.getContentType());

		int contentLength = this.getContentLength();
		if (!HttpConnection.GET.equals(connectionMethod) && contentLength != -1) {
			conn.setRequestProperty("Content-Length", String.valueOf(contentLength));
		}
			
		// any others
		Enumeration keys = requestProps.getOtherProperties().keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) requestProps.getOtherProperties().get(key);
			conn.setRequestProperty(key, value);
		}

		return conn;

	}

	
	
	
	
	public String toString() {
		String s = "#" + getCacheIdentifier() + " (http)";
		if (getResponseCode() > 0)
			s += " " + getResponseCode();
		return s;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
		url = ExtUtil.readString(in);
		responseCode = (int)ExtUtil.readNumeric(in);
		responseBody = ExtUtil.nullIfEmpty(ExtUtil.readBytes(in));
		content = ExtUtil.readBytes(in);
	}
		
	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		ExtUtil.writeString(out,url);
		ExtUtil.writeNumeric(out,responseCode);
		ExtUtil.writeBytes(out, ExtUtil.emptyIfNull(responseBody));
		ExtUtil.writeBytes(out, content);
	}
	
}
