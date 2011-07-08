//#condition polish.api.wmapi

package org.javarosa.services.transport.impl.sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.impl.BasicTransportMessage;
import org.javarosa.services.transport.impl.TransportMessageStatus;

/**
 * 
 * Wireless Messaging API (JSR-120 or WMA)
 * 
 * 
 * SMS message object
 * 
 * Since the message to be sent may require to be partitioned into more than one
 * SMS payloads, the content of the SMSTransportMessage is a Vector of Strings
 * (in the simplest case, vector size = 1)
 * 
 * 
 */
public class SMSTransportMessage extends BasicTransportMessage {

	/**
	 * SMS messages can be no longer than 160 bytes in length
	 */
	public final static int MAX_SMS_BYTES = 140;

	/**
	 * 
	 */
	private String destinationURL;
	
	private Vector content;

	/**
	 * NOTE: DO NOT USE. ONLY FOR SERIALIZATION
	 */
	public SMSTransportMessage() {
		//ONLY FOR SERIALIZATION
	}
	
	/**
	 * @param str
	 * @param destinationURL
	 * @throws UnsupportedEncodingException 
	 */
	public SMSTransportMessage(String payload, String destinationURL) throws UnsupportedEncodingException {
		this.destinationURL = destinationURL;
		
		//Try to convert the string to ascii by outputting UTF-8 and re-importing it. Incompatible
		//(non-7-bit) characters will be read in as extra chars
		String asciiString = new String(payload.getBytes("UTF-8"),"ASCII");
		//A UTF-16 string should always represent the string properly, so it's our gold standard
		String unicodeString = new String(payload.getBytes("UTF-16BE"),"UTF-16BE");
		
		if(asciiString.length() != unicodeString.length()) {
			content = splitSMS(payload, "UTF-16BE");
		} else {			
			content = splitSMS(payload, "ASCII");
		}
	}
	
	public boolean isCacheable() {
		return false;
	}
	public boolean isShareTransporter(){
		return false;
	}
	

	public Object getContent() {
		return content;
	}

	/**
	 * 
	 * SMS messages must be 160 bytes or less each.
	 *  
	 * If the message to be sent is greater, it is partitioned.
	 * 
	 * @param str
	 * @return Vector of strings to be sent as separate messages
	 * @throws UnsupportedEncodingException 
	 */
	private Vector splitSMS(String str, String encoding) throws UnsupportedEncodingException {
		Vector v = new Vector();
		
		String currentMessage = "";

		//TODO: This might take a _looooooooooooong_ time if our message is huge. Test.
		
		// Go through one char at a time building output strings in
		// the given encoding
		for(int i = 0 ; i < str.length() ; ++i) {
			if((currentMessage + str.charAt(i)).getBytes(encoding).length <= MAX_SMS_BYTES) {
				currentMessage += str.charAt(i);
			} else {
				v.addElement(currentMessage);
				currentMessage = "";
				currentMessage += str.charAt(i);
			}
		}
		
		v.addElement(currentMessage);
		
		
		return v;
	}

	/**
	 * @return
	 */
	public String getDestinationURL() {
		return destinationURL;
	}

	/**
	 * @param destinationURL
	 */
	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}
	
	public void send() {
		MessageConnection conn = null;
		try {
			System.out.println("SMSTransporter.send() - destination = " + this.getDestinationURL());

			// create a MessageConnection
			conn = getConnection(this.getDestinationURL());
			
			//int segments = conn.numberOfSegments(message);
			

			// the SMS content has been split into n parts of not more than 140
			// characters
			Vector messageParts = (Vector) this.getContent();

			for (int i = 0; i < messageParts.size(); i++) {
				String smsContent = (String) messageParts.elementAt(i);
				System.out.println("Sending: " + smsContent);
				sendMessage(smsContent, conn);
			}
			this.setStatus(TransportMessageStatus.SENT);

		} catch (Exception e) {
			System.out.println("Connection failed: ");
			this.setFailureReason(e.getMessage());
			this.incrementFailureCount();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (IOException e) {
					// do nothing
				}
		}
	}

	/**
	 * 
	 * Send single sms over a MessageConnection
	 * 
	 * @param content The content of the SMS to be sent
	 * @param conn The connection over which the SMS is to be sent
	 * @throws IOException
	 */
	private void sendMessage(String content, MessageConnection conn) throws IOException {
		TextMessage sms = (TextMessage) conn.newMessage(MessageConnection.TEXT_MESSAGE);
		sms.setAddress(this.getDestinationURL());
		sms.setPayloadText(content);
		conn.send(sms);
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private static MessageConnection getConnection(String url) throws IOException {
		Object o = Connector.open(url);
		if (o instanceof MessageConnection)
			return (MessageConnection) o;
		else
			throw new IllegalArgumentException("Not SMS URL:" + url);
	}

	
	
	
	
	
	
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
		content = (Vector)ExtUtil.read(in, new ExtWrapList(String.class));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		ExtUtil.write(out, new ExtWrapList(content));
	}

}
