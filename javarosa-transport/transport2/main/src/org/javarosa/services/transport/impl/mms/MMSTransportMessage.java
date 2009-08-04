package org.javarosa.services.transport.impl.mms;

import javax.wireless.messaging.MessagePart;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

public class MMSTransportMessage extends BasicTransportMessage implements
		Serializable {

	public MMSTransportMessage(String phoneNumber, String subject,
			String priority, MessagePart[] parts, String applicationID) {
		setPhoneNumber(phoneNumber);
		setSubject(subject);
		setPriority(priority);
		setApplicationID(applicationID);
		setContent(parts);
	}

	/**
	 * 
	 */
	private String phoneNumber;

	private String subject;
	private String priority; // "high", "normal" or "low"

	private String applicationID;

	public String getApplicationID() {
		return applicationID;
	}

	public void setApplicationID(String applicationID) {
		this.applicationID = applicationID;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new MMSTransporter(this);
	}

}
