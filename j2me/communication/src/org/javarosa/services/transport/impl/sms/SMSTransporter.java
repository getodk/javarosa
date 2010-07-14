package org.javarosa.services.transport.impl.sms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportMessageStatus;

/**
 * An SMSTransporter can send the SMSTransportMessage passed to it in its
 * constructor
 * 
 */
public class SMSTransporter implements Transporter {
	/**
	 * The message to be sent by this transporter
	 */
	private SMSTransportMessage message;

	/**
	 * @param message The message to be sent
	 */
	public SMSTransporter(SMSTransportMessage message) {
		 
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.Transporter#getMessage()
	 */
	public TransportMessage getMessage() {
		return this.message;
	}
	public void setMessage(TransportMessage m) {
		this.message = (SMSTransportMessage)m;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.Transporter#send()
	 */
	public TransportMessage send() {
		MessageConnection conn = null;
		try {

			// create a MessageConnection
			conn = getConnection(message.getDestinationURL());
			
			//int segments = conn.numberOfSegments(message);
			

			// the SMS content has been split into n parts of not more than 140
			// characters
			Vector messageParts = (Vector) this.message.getContent();

			for (int i = 0; i < messageParts.size(); i++) {
				String smsContent = (String) messageParts.elementAt(i);
				sendMessage(smsContent, conn);
			}
			message.setStatus(TransportMessageStatus.SENT);

		} catch (Exception e) {
			System.out.println("Connection failed: ");
			message.setFailureReason(e.getMessage());
			message.incrementFailureCount();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (IOException e) {
					// do nothing
				}
		}

		return message;
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
		TextMessage sms = (TextMessage) conn
				.newMessage(MessageConnection.TEXT_MESSAGE);
		sms.setAddress(message.getDestinationURL());
		sms.setPayloadText(content);
		conn.send(sms);
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private MessageConnection getConnection(String url) throws IOException {
		Object o = Connector.open(url);
		if (o instanceof MessageConnection)
			return (MessageConnection) o;
		else
			throw new IllegalArgumentException("Not SMS URL:" + url);

	}
}
