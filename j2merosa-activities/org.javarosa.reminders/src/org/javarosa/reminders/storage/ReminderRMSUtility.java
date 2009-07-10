/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.reminders.storage;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.externalizable.DeserializationException;
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
	
	public Reminder retrieveFromRMS(int reminderId) throws IOException, IllegalAccessException, InstantiationException, DeserializationException {
		Reminder reminder = new Reminder();
		
		this.retrieveFromRMS(reminderId, reminder);
		
		return reminder;
	}
	
	public Vector getReminders() throws IOException, IllegalAccessException,
			InstantiationException, DeserializationException {
		IRecordStoreEnumeration en = this.enumerateMetaData();
		Vector reminders = new Vector();
		try {
			while (en.hasNextElement()) {
				int id;
				id = en.nextRecordId();
				Reminder reminder = new Reminder();
				super.retrieveFromRMS(id, reminder);
				reminders.addElement(reminder);
			}
		} catch (RecordStorageException e) {
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
