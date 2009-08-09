 package org.javarosa.services.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class HttpConnectionROSA {

	private HttpConnection connection;
	private OutputStream out;
	

	public HttpConnectionROSA(String url)
			throws IOException {
		super();
		this.connection = (HttpConnection) Connector.open(url,Connector.READ_WRITE);

		this.connection.setRequestMethod(HttpConnection.POST);
		this.connection.setRequestProperty("User-Agent",
				"Profile/MIDP-2.0 Configuration/CLDC-1.1");
		this.connection.setRequestProperty("Content-Language", "en-US");
		this.connection.setRequestProperty("MIME-version", "1.0");
		this.connection.setRequestProperty("Content-Type", "text/plain");

		this.out = this.connection.openOutputStream();
	}

	public HttpConnection getConnection() {
		return this.connection;
	}
	
	

	public OutputStream getOut() {
		return out;
	}

	public byte[] readResponse() throws IOException {
		
		InputStream in = getConnection().openInputStream();

		// Get the length and process the data
		byte[] data;
		int len = (int) getConnection().getLength();
		int read;
		if (len >= 0) {
			data = new byte[len];
			read = 0;
			while (read < len) {
				int k = in.read(data, read, len - read);
				if (k == -1)
					break;
				read += k;
			}
		} else {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			while (true) {
				int b = in.read();
				if (b == -1) {
					break;
				}
				buffer.write(b);
			}
			data = buffer.toByteArray();
			read = data.length;
		}

		System.out.println(read + " bytes read");
		if (len > 0 && read < len) {
			System.out.println("WARNING: expected " + len + "!!");
		}
		// replyS
		System.out.println(new String(data, "UTF-8"));

		return data;
	}

	
	
//	private void cleanStreams() {
//		if (this.in != null) {
//			try {
//				this.in.close();
//			} catch (IOException e) {
//				// ignore
//			}
//		}
//		if (this.out != null) {
//			try {
//				this.out.close();
//			} catch (IOException e) {
//				// ignore
//			}
//		}
//		if (this.conn.getConnection() != null) {
//			try {
//				this.conn.getConnection().close();
//			} catch (IOException e) {
//				// ignore
//			}
//		}
//
//	}

}
