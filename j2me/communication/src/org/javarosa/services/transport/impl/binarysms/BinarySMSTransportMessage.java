//#condition polish.api.wmapi

package org.javarosa.services.transport.impl.binarysms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.impl.BasicTransportMessage;

/**
 * SMS message object
 * 
 * Since the message to be sent may require to be partitioned into more than one
 * SMS payloads, the content of the SMSTransportMessage is a Vector of Strings
 * (in the simplest case, vector size = 1)
 * 
 * 
 */
public class BinarySMSTransportMessage extends BasicTransportMessage {
		
	byte[] raw;
	Vector<byte[]> content;

	/**
	 * 
	 */
	private String destinationURL;

	/**
	 * FOR DESERIALIZATION ONLY!
	 */
	public BinarySMSTransportMessage() {
		//ONLY FOR DESERIALIZING
	}
	
	/**
	 * @param str
	 * @param destinationURL
	 * @throws UnsupportedEncodingException 
	 */
	public BinarySMSTransportMessage(String str, String destinationURL) throws UnsupportedEncodingException {
		this.destinationURL = destinationURL;
		setContent(str.getBytes("UTF-16"));
	}
	
	/**
	 * @param str
	 * @param destinationURL
	 */
	public BinarySMSTransportMessage(byte[] bytes, String destinationURL) {
		this.destinationURL = destinationURL;
		setContent(bytes);
	}
	
	private void setContent(byte[] bytes) {
		raw = bytes;
		content = new Vector<byte[]>();
		for(int i = 0 ; i <= bytes.length / 140; ++i) {
			int chunk = 140;
			if(i == bytes.length / 140) {
				//Last chunk, doesn't need to be as long 
				chunk = bytes.length % 140;
			}
			if(chunk == 0) {
				continue;
			}
			
			byte[] message = new byte[chunk];
			
			for(int j = 0 ; j < message.length ; ++j) {
				message[j] = bytes[i*140 + j]; 
			}
			content.addElement(message);
		}
	}

	public boolean isCacheable() {
		return true;
	}
	
	public Object getContent() {
		return content;
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
			System.out.println("BinarySMSTransporter.send() - destination = " + this.getDestinationURL());

			// create a MessageConnection
			conn = getConnection(this.getDestinationURL());
			Vector<byte[]> data = (Vector<byte[]>)this.getContent();
			for(byte[] messageContent : data) {
				sendMessage(messageContent, conn);
			}

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
	 * @param content
	 *            The content of the SMS to be sent
	 * @param conn
	 *            The connection over which the SMS is to be sent
	 * @throws IOException
	 */
	private void sendMessage(byte[] content, MessageConnection conn) throws IOException {
		BinaryMessage sms = (BinaryMessage) conn.newMessage(MessageConnection.BINARY_MESSAGE);
		sms.setAddress(this.getDestinationURL());
		sms.setPayloadData(content);
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
	
	
	
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		super.readExternal(in, pf);
		destinationURL = ExtUtil.readString(in);
		setContent(ExtUtil.readBytes(in));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		ExtUtil.writeString(out, destinationURL);
		ExtUtil.writeBytes(out, raw);
	}

}
