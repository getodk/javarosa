package org.javarosa.services.transport.impl.mms;

import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

import de.enough.polish.io.Serializable;

 
public class MMSTransportMessage extends BasicTransportMessage implements
		Serializable {

 
	public MMSTransportMessage(String str, String address,String subject,String priority,String contentId,String contentLocation,String contentMimeType) {
		setAddress(address);
		setSubject(subject);
		setPriority(priority);
		setContentId(contentId);
		setContentLocation(contentLocation);
		setContentMimeType(contentMimeType);
	}

	 

	/**
	 * 
	 */
	private String address;
	private String subject;
	private String priority; // "high", "normal" or "low"
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



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new MMSTransporter(this);
	}

}
