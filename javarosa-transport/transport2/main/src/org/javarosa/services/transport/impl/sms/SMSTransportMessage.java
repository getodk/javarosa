package org.javarosa.services.transport.impl.sms;

import java.util.Vector;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

/**
 * SMS transport - content is a Vector of strings 
 *
 */
public class SMSTransportMessage extends BasicTransportMessage implements
		Serializable {

	public final static int MAX_SIZE = 140;

	private String destinationURL;

	public SMSTransportMessage(String str, String destinationURL) {
		this.destinationURL = destinationURL;
		setContent(splitSMS(str));
	}

	private Vector splitSMS(String str) {
		String message = str;
		Vector v = new Vector();
		
		// if message is too long split it
		while (message.length() > MAX_SIZE) {
			String part = message.substring(0, MAX_SIZE);
			v.addElement(part);
			message = message.substring(MAX_SIZE + 1);
		}
		
		// whatever remaining must also be added
		if (message.length() > 0)
			v.addElement(message);
		return v;
	}

	public String getDestinationURL() {
		return destinationURL;
	}

	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new SMSTransporter(this);
	}

}
