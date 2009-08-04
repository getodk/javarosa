package org.javarosa.services.transport.impl.mms;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessagePart;
import javax.wireless.messaging.MultipartMessage;
import javax.wireless.messaging.SizeExceededException;

import org.javarosa.services.transport.ITransporter;
import org.javarosa.services.transport.message.TransportMessage;

/**
 * An MMSTransporter can send the MMSTransportMessage passed to it in its
 * constructor
 * 
 */
public class MMSTransporter implements ITransporter {
	/**
	 * The message to be sent by this transporter
	 */
	private MMSTransportMessage message;

	/**
	 * @param message
	 *            The message to be sent
	 */
	public MMSTransporter(MMSTransportMessage message) {
		super();
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

	public TransportMessage send() {
		try {
			// create a MessageConnection
			String address = "mms://" + message.getPhoneNumber();
			if (message.getApplicationID() != null)
				address += ":" + message.getApplicationID();
			MessageConnection conn = getConnection(address);
			//conn.setMessageListener(this);

			// Prepare the multipart message
			MultipartMessage mm = (MultipartMessage) conn
					.newMessage(MessageConnection.MULTIPART_MESSAGE);

			// Set the destination address
			mm.setAddress("mms://" + message.getPhoneNumber());

			// Set the subject

			mm.setSubject(this.message.getSubject());

			// Set the priority
			mm.setHeader("X-Mms-Priority", this.message.getPriority());

			// Set the message part
			MessagePart[] parts = (MessagePart[])this.message.getContent();

			for (int i = 0; i < parts.length; i++) {
				mm.addMessagePart(parts[i]);
			}

		} catch (SizeExceededException ex) {
			// TODO: Exception handling
		} catch (IOException ex) {
			// TODO: Exception handling
		}

		return this.message;

	}


	public TransportMessage fetch(){
		throw new RuntimeException("No fetch defined for MMS");
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
