package org.javarosa.reminders.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

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

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		reminderId = in.readInt();
		
		followUpDate = (Date)ExtUtil.read(in, new ExtWrapNullable(Date.class));
		patientId = in.readInt();
		patientName = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
		title = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
		text = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
		notified = in.readBoolean();
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(reminderId);
		
		ExtUtil.write(out, new ExtWrapNullable(followUpDate));
		out.writeInt(patientId);
		ExtUtil.write(out, new ExtWrapNullable(patientName));
		ExtUtil.write(out, new ExtWrapNullable(title));
		ExtUtil.write(out, new ExtWrapNullable(text));
		out.writeBoolean(notified);
	}	
}
