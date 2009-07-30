package org.javarosa.services.transport.impl.mms;

import java.util.Vector;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

 
public class MMSTransportMessage extends BasicTransportMessage implements
		Serializable {

	/**
	 * SMS messages can be no longer than 140 characters in length
	 */
	public final static int MAX_SMS_LENGTH = 140;

	/**
	 * 
	 */
	private String address;
	private String subject;
	private String priority;
	private String contentId;
	private String contentLocation;
	private String contentMimeType;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getContentLocation() {
		return contentLocation;
	}

	public void setContentLocation(String contentLocation) {
		this.contentLocation = contentLocation;
	}

	public String getContentMimeType() {
		return contentMimeType;
	}

	public void setContentMimeType(String contentMimeType) {
		this.contentMimeType = contentMimeType;
	}

	/**
	 * @param str
	 * @param destinationURL
	 */
	public MMSTransportMessage(String str, String address) {
		setAddress(address);
		setContent(splitSMS(str));
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

 

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new MMSTransporter(this);
	}

}
