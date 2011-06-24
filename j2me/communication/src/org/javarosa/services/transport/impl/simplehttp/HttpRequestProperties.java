package org.javarosa.services.transport.impl.simplehttp;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.microedition.io.HttpConnection;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.locale.Localization;

public class HttpRequestProperties {
	
	private String requestMethod = HttpConnection.POST;
	
	private Hashtable<String, String> properties;
	
	private long date;
	
	public static HttpRequestProperties HttpResponsePropertyFactory(HttpConnection conn) throws IOException {
		HttpRequestProperties ret = new HttpRequestProperties();
		
		String[] expectedHeaders = new String [] { "Content-Language", "X-OpenRosa-Version"};
		
		ret.date = conn.getHeaderFieldDate("Date", 0);
		if(ret.date == 0) {
			ret.date = conn.getHeaderFieldDate("date", 0);
		}
		
		for(String header : expectedHeaders) {
			ret.setRequestProperty(header, getHeaderHelper(conn, header));
		}
		return ret;
	}
	
	/**
	 * j2me often lowercases everything, so we need to try the "correct" name
	 * and the lowercase name.
	 * 
	 * @param conn
	 * @param value
	 * @return
	 */
	private static String getHeaderHelper(HttpConnection conn, String value)  throws IOException {
		String ret = conn.getHeaderField(value);
		
		return ret == null ? conn.getHeaderField(value.toLowerCase()) : ret; 
	}
	
	/** 
	 * For reponse properties
	 */
	private HttpRequestProperties() {
		properties = new Hashtable<String, String>();
		date = 0;
	}
	
	public HttpRequestProperties(String method, int contentLength) {
		this(method, contentLength, null);
	}
	
	public HttpRequestProperties(String method, int contentLength, String orApiVersion) {
		
		properties = new Hashtable<String, String>();
		
		requestMethod = method;
		
		properties.put("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.1");
		//TODO: Be more clever than this
		properties.put("Content-Language", "en-US");
		properties.put("MIME-version", "1.0");
		//TODO: Multipart messages
		properties.put("Content-Type", "text/xml");
		
		if(orApiVersion != null) {
			properties.put("X-OpenRosa-Version",orApiVersion);
		}
		
		String acceptLanguage = Localization.getGlobalLocalizerAdvanced().getLocale();
		if(acceptLanguage != null) {
			properties.put("Accept-Language", acceptLanguage);
		}
		
		if(!HttpConnection.GET.equals(method) && contentLength != -1) {
			properties.put("Content-Length", String.valueOf(contentLength));
		}
	}
	
	 
	public void setRequestProperty(String property,String value){
		if(value == null) {
			//remove property?
		} else {
			properties.put(property, value);
		}
	}
	public Hashtable getOtherProperties() {
		return properties;
	}
	
	public String getORApiVersion() {
		return properties.get("X-OpenRosa-Version");
	}
	
	public String getRequestMethod() {
		return requestMethod;
	}
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}
	public String getUserAgent() {
		return properties.get("User-Agent");
	}
	public void setUserAgent(String userAgent) {
		properties.put("User-Agent", userAgent);
	}
	public String getContentLanguage() {
		return properties.get("Content-Language");
	}
	public void setContentLanguage(String contentLanguage) {
		properties.put("Content-Language", contentLanguage);
	}
	public String getMimeVersion() {
		return properties.get("MIME-version");
	}
	public void setMimeVersion(String mimeVersion) {
		properties.put("MIME-version", mimeVersion);
	}
	public String getContentType() {
		return properties.get("Content-Type");
	}
	public void setContentType(String contentType) {
		properties.put("Content-Type", contentType);
	}
	
	public long getGMTDate() {
		return date;
	}
	
	public void configureConnection(HttpConnection conn) throws IOException {
		conn.setRequestMethod(requestMethod);
		
		for(Enumeration en = properties.keys() ; en.hasMoreElements() ;) {
			String key = (String)en.nextElement();
			String val = properties.get(key);
			
			conn.setRequestProperty(key, val);
		}

		conn.setRequestProperty("Date", DateUtils.formatDateTime(new Date(), DateUtils.FORMAT_TIMESTAMP_HTTP));
	}

}
