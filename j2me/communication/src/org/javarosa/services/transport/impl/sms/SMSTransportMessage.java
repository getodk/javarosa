//#condition polish.api.wmapi

package org.javarosa.services.transport.impl.sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
	 * SMS messages can be no longer than 140 characters in length
	 */
	public final static int MAX_SMS_LENGTH = 140;

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
	 */
	public SMSTransportMessage(String str, String destinationURL) {
		this.destinationURL = destinationURL;
		content = splitSMS(str);
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
	 * SMS can be of maximum 140 characters in length.
	 * 
	 * If the message to be sent is greater, it is partitioned.
	 * 
	 * @param str
	 * @return Vector of strings to be sent as separate messages
	 */
	private Vector splitSMS(String str) {
		String message = str;
		Vector v = new Vector();

		// if message is too long split it
		while (message.length() > MAX_SMS_LENGTH) {
			String part = message.substring(0, MAX_SMS_LENGTH);
			v.addElement(part);
			message = message.substring(MAX_SMS_LENGTH + 1);
		}

		// whatever remaining of the message after
		// chopping out 140 character length chunks
		// must also be added
		if (message.length() > 0)
			v.addElement(message);
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
