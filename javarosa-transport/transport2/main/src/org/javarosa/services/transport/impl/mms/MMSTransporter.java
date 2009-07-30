package org.javarosa.services.transport.impl.mms;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessagePart;
import javax.wireless.messaging.MultipartMessage;
import javax.wireless.messaging.SizeExceededException;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;

/**
 * An SMSTransporter can send the SMSTransportMessage passed to it in its
 * constructor
 * 
 */
public class MMSTransporter implements Transporter {
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
			MessageConnection conn = getConnection(message.getAddress());

			// Prepare the multipart message
			MultipartMessage mm = (MultipartMessage) conn
					.newMessage(MessageConnection.MULTIPART_MESSAGE);

			// Set the destination address
			mm.setAddress(this.message.getAddress());

			// Set the subject

			mm.setSubject(this.message.getSubject());

			// Set the priority
			String priority = "normal"; // "high", "normal" or "low"
			mm.setHeader("X-Mms-Priority", this.message.getPriority());

			// Set the message part

			MessagePart messagePart = createMsgPart();
			mm.addMessagePart(messagePart);
		} catch (SizeExceededException ex) {
			// TODO: Exception handling
		} catch (IOException ex) {
			// TODO: Exception handling
		}

		return this.message;

	}

	/**
	 * Constructs a MessagePart which can be added to a MultipartMessage.
	 * 
	 * @return the constructed MessagePart
	 * @throws javax.wireless.messaging.SizeExceededException
	 *             if the contents is larger than the available memory or
	 *             supported size for the message part
	 * @throws java.io.IOException
	 *             if the resource cannot be read
	 */
	private MessagePart createMsgPart() throws SizeExceededException,
			IOException {
		String imageContentID = this.message.getContentId();
		String imageContentLocation = this.message.getContentLocation();
		String jpgMIME = this.message.getContentMimeType();
		InputStream imageContent = getClass().getResourceAsStream(
				imageContentLocation);
		MessagePart messagePart = new MessagePart(imageContent, jpgMIME,
				imageContentID, imageContentLocation, null);
		return messagePart;
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
