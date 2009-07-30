package org.javarosa.services.transport.impl.sms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;

public class SMSTransporter implements Transporter {
	private SMSTransportMessage message;

	public SMSTransporter(SMSTransportMessage message) {
		super();
		this.message = message;
	}

	public TransportMessage getMessage() {
		return this.message;
	}

	public TransportMessage send() {
		MessageConnection conn = null;
		try {
			conn = (MessageConnection) Connector.open(message
					.getDestinationURL());
			Vector messageParts = (Vector) this.message.getContent();

			for (int i = 0; i < messageParts.size(); i++) {

				TextMessage tmsg = (TextMessage) conn
						.newMessage(MessageConnection.TEXT_MESSAGE);
				tmsg.setAddress(message.getDestinationURL());

				tmsg.setPayloadText((String) messageParts.elementAt(i));
				conn.send(tmsg);
			}

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

}
