package org.javarosa.reminders.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

public class Reminder implements Externalizable, IDRecordable {
	Date followUpDate;
	
	int patientId;
	String patientName;
	
	String title;
	String text;
	
	boolean notified;
	
	int reminderId;
	
	public Reminder() {
		
	}
	
	public Reminder(Date followUpDate, int patientId, String patientName, String title, String text) {
		this(followUpDate, patientId, patientName, title, text, false);
	}
	
	public Reminder(Date followUpDate, int patientId, String patientName, String title, String text, boolean notified) {
		this.followUpDate = followUpDate;
		this.patientId = patientId;
		this.patientName = patientName;
		this.title = title;
		this.text = text;
		this.notified = notified;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.IDRecordable#setRecordId(int)
	 */
	public void setRecordId(int recordId) {
		reminderId = recordId;
	}
	
	public int getRecordId() {
		return reminderId;
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

	/**
	 * @return the notified
	 */
	public boolean isNotified() {
		return notified;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param notified the notified to set
	 */
	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	/**
	 * @return the patientName
	 */
	public String getPatientName() {
		return patientName;
	}

	/**
	 * @param patientName the patientName to set
	 */
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		followUpDate = ExternalizableHelper.readDate(in);
		patientId = in.readInt();
		patientName = ExternalizableHelper.readUTF(in);
		title = ExternalizableHelper.readUTF(in);
		text = ExternalizableHelper.readUTF(in);
		notified = in.readBoolean();
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeDate(out, followUpDate);
		out.writeInt(patientId);
		ExternalizableHelper.writeUTF(out, patientName);
		ExternalizableHelper.writeUTF(out, title);
		ExternalizableHelper.writeUTF(out, text);
		out.writeBoolean(notified);
	}	
}
