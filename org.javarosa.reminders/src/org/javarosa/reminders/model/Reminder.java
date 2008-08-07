package org.javarosa.reminders.model;

import java.util.Date;

public class Reminder {
	Date followUpDate;
	int patientId;
	String text;
	
	public Reminder() {
		
	}
	
	public Reminder(Date followUpDate, int patientId, String text) {
		this.followUpDate = followUpDate;
		this.patientId = patientId;
		this.text = text;
	}

	/**
	 * @return the followUpDate
	 */
	public Date getFollowUpDate() {
		return followUpDate;
	}

	/**
	 * @param followUpDate the followUpDate to set
	 */
	public void setFollowUpDate(Date followUpDate) {
		this.followUpDate = followUpDate;
	}

	/**
	 * @return the patientId
	 */
	public int getPatientId() {
		return patientId;
	}

	/**
	 * @param patientId the patientId to set
	 */
	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
}
