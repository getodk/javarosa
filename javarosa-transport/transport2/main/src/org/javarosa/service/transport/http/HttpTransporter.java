/**
 * 
 */
package org.javarosa.service.transport.http;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.core.util.Queue;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.StreamsUtil;

/**
 * @author ctsims
 *
 */
public class HttpTransporter implements Transporter, Runnable {
	
	/** In order to effectively queue, we want one transporter in memory at a time. **/
	static HttpTransporter activeTransporter;
	
	/** Queue<HttpTransportMessage> **/
	private Queue messageQueue;
	
	private Thread sendingThread; 
	
	private HttpTransportMessage currentMessage;
	
	public HttpTransporter() {
		messageQueue = new Queue();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.Transporter#send()
	 */
	public void send(TransportMessage message) {
		messageQueue.queue(message);
		triggerQueue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.services.transport.Transporter#getTransporter()
	 */
	public Transporter getTransporter() {
		if(activeTransporter != null) {
			activeTransporter = new HttpTransporter();
		}
		return activeTransporter;
	}
	
	/**
	 * Triggers the thread for sending to ensure that one is active.
	 */
	private void triggerQueue() {
		if(!messageQueue.empty()) {
			if(sendingThread != null && sendingThread.isAlive()) {
				//Do nothing. We don't want to screw with the things
				//being sent currently.
			} else {
				//We've set the message we want to send, so start the queue to try to send it.
				sendingThread = new Thread(this);
				sendingThread.start();
			}
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.services.transport.Transporter#stop()
	 */
	public void stop() {
		//TODO: Implement a method to stop sending and wipe the queue
	}
	
	/** Begin methods occuring inside of the thread **/

	
	public void run() {
		while(!messageQueue.empty()) {
			currentMessage = (HttpTransportMessage)messageQueue.poll();
			if(currentMessage.isReady()) {
				sendThread(currentMessage);
				if(currentMessage.getStatus() != TransportListener.SUCCESS && currentMessage.getAttemptsLeft() == 0) {
					//We're out of attempts. Let the thread expire if necessary, but don't attempt to 
					//resend this element.
				} else {
					messageQueue.queue(currentMessage);
				}
			} else {
				messageQueue.queue(currentMessage);
			}
		}
	}
	
	/**
	 * Send the given message and deal with the results if a failure occurs.
	 * 
	 * @param message The message to be sent
	 */
	private void sendThread(HttpTransportMessage message) {
		HttpConnection conn = null;

		try {
			conn = getConnection(message.getDestinationURL());

			StreamsUtil.writeFromInputToOutput(message.getContentStream(), conn.openOutputStream());

			readResponse(conn, message);
			
			conn.close();
		} catch (IOException e) {
			System.out.println("Connection failed: ");
			message.fail("Http transport attempt failed with exception: " + e.getMessage(), TransportListener.FAILURE_TRANSPORTING);
		} catch(IllegalArgumentException iae) {
			message.fail(iae.getMessage(), TransportListener.FAILURE_DESTINATION, true);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}
	
	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
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


	/**
	 * 
	 * Read the response from the HttpConnection and record
	 * in the SimpleHttpTransportMessage
	 * 
	 * 
	 * @param conn
	 * @param result
	 * @return
	 * @throws IOException
	 * @throws ClassCastException
	 */
	private HttpTransportMessage readResponse(HttpConnection conn,
			HttpTransportMessage message) throws IOException {

		int responseCode = conn.getResponseCode();
		if (responseCode == HttpConnection.HTTP_OK) {
			byte[] response;
			try {
			    response  = readResponseBody(conn);
			} catch(IOException e) {
				throw new IOException("Error while reading response from server after positive response. Error is: " + e.getMessage());
			}
			message.setSuccesfulResponse(response);
		} else {
			message.fail("Error received from server response. Response code was HttpConnection: " + conn.getResponseCode() + " if this response code should be valid. Update readResponse() in HttpTransporter", responseCode);
		}

		return message;

	}

	/**
	 * 
	 * 
	 * 
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	private byte[] readResponseBody(HttpConnection conn) throws IOException {
		InputStream in = null;
		byte[] response;
		try {
			in = conn.openInputStream();
			int len = (int) conn.getLength();
			response = StreamsUtil.readFromStream(in, len);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return response;
	}
}
