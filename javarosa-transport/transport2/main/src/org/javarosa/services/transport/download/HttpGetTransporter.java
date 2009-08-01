package org.javarosa.services.transport.download;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.listeners.IGetTransportMessage;
import org.javarosa.services.transport.listeners.IGetTransporter;
import org.javarosa.services.transport.util.StreamsUtil;

public class HttpGetTransporter implements IGetTransporter {

	private Date downloadWatchDate;
	private HttpGetTransportMessage message;
	
	public HttpGetTransporter() {
		
	}
	
	public IGetTransportMessage get() {
		
		byte[] data = null;
		HttpConnection conn = null;
		try{
			conn = getConnection(message.geDestinationURL());
			
			if(conn.getResponseCode() == HttpConnection.HTTP_OK){
				data = readFromConnection(conn);
				
				message = setMessageDetail(message, data);
			}
			
			conn.close();
			
		}catch(Exception e){
			System.out.println("Connection to the server has failed: " + e.getMessage());
			message.setFailureReason(e.getMessage());
		}
		finally{
			if(conn != null)
				try{
					conn.close();
				}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		
		return message;
	}

	private HttpGetTransportMessage setMessageDetail(HttpGetTransportMessage message, byte[] data) {
		
		HttpGetTransportMessage downloadedMessage = message;
		
		downloadedMessage.setReturnedContent(data);
		downloadedMessage.setDownloadedDate(downloadWatchDate.getTime());
		downloadedMessage.setStatus(TransportMessageStatus.DOWNLOADED);
		
		return downloadedMessage;
	}

	private byte[] readFromConnection(HttpConnection conn) throws Exception {
		
		byte[] data = null;
		DataInputStream in = null;
		try{
			in = conn.openDataInputStream();
			System.out.println("Reading from URL: " + conn.getURL());
			
			data = StreamsUtil.readFromStream(in);
			
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		finally{
			if(in != null){
				in.close();
			}
		}
		
		return data;
		
	}

	private HttpConnection getConnection(String url) throws IOException {
		
		HttpConnection conn;
		Object o = Connector.open(url);
		if (o instanceof HttpConnection) {
			conn = (HttpConnection) o;
			conn.setRequestMethod(HttpConnection.POST);
			conn.setRequestProperty("User-Agent",
					"Profile/MIDP-2.0 Configuration/CLDC-1.1");
			conn.setRequestProperty("Content-Language", "en-US");
			conn.setRequestProperty("MIME-version", "1.0");
			conn.setRequestProperty("Content-Type", "text/plain");
		} else {
			throw new IllegalArgumentException("Not HTTP URL:" + url);
		}
		return conn;

	}
}
