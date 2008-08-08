package org.javarosa.reminders.storage;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.reminders.model.Reminder;

public class ReminderRMSUtility extends RMSUtility {
	public ReminderRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_STANDARD);
	}
	
	public static String getUtilityName() {
		return "REMINDER_RMS_UTILITY";
	}
	
	/**
	 * Writes the given form data to persistent storage
	 * @param form The form to be written
	 */
	public void writeToRMS(Reminder reminder) {
		reminder.setRecordId(this.getNextRecordID());
		super.writeToRMS(reminder, null);
	}
	
	public Reminder retrieveFromRMS(int reminderId) throws IOException, IllegalAccessException, InstantiationException, UnavailableExternalizerException {
		Reminder reminder = new Reminder();
		
		this.retrieveFromRMS(reminderId, reminder);
		
		return reminder;
	}
	
	public Vector getReminders() throws IOException, IllegalAccessException,
			InstantiationException, UnavailableExternalizerException {
		RecordEnumeration en = this.enumerateMetaData();
		Vector reminders = new Vector();
		try {
			while (en.hasNextElement()) {
				int id;
				id = en.nextRecordId();
				Reminder reminder = new Reminder();
				super.retrieveFromRMS(id, reminder);
				reminders.addElement(reminder);
			}
		} catch (InvalidRecordIDException e) {
			throw new IOException("There was a problem with the Reminder RMS trying to enumerate all of its members. This really shouldn't happen.");
		}
		return reminders;
	}

	public void updateReminder(Reminder reminder) {
		this.updateToRMS(reminder.getRecordId(), reminder, null);
	}
	
	public void removeReminder(Reminder reminder) {
		this.deleteRecord(reminder.getRecordId());
	}
}
