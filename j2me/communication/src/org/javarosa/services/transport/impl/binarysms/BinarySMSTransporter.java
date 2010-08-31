package org.javarosa.services.transport.impl.binarysms;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;

/**
 * An SMSTransporter can send the SMSTransportMessage passed to it in its
 * constructor
 * 
 */
public class BinarySMSTransporter implements Transporter {
	/**
	 * The message to be sent by this transporter
	 */
	private BinarySMSTransportMessage message;

	/**
	 * @param message
	 *            The message to be sent
	 */
	public BinarySMSTransporter(BinarySMSTransportMessage message) {

		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.Transporter#getMessage()
	 */
	public TransportMessage getMessage() {
		return this.message;
	}

	public void setMessage(TransportMessage m) {
		this.message = (BinarySMSTransportMessage) m;
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

			sendMessage((byte[]) message.getContent(), conn);

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
	 * @param content
	 *            The content of the SMS to be sent
	 * @param conn
	 *            The connection over which the SMS is to be sent
	 * @throws IOException
	 */
	private void sendMessage(byte[] content, MessageConnection conn)
			throws IOException {
		BinaryMessage sms = (BinaryMessage) conn
				.newMessage(MessageConnection.BINARY_MESSAGE);
		sms.setAddress(message.getDestinationURL());
		sms.setPayloadData(content);
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
