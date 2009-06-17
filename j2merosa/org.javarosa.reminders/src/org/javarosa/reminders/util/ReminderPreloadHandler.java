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

package org.javarosa.reminders.util;

import java.util.Date;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.patient.model.Patient;
import org.javarosa.reminders.model.Reminder;
import org.javarosa.reminders.storage.ReminderRMSUtility;

public class ReminderPreloadHandler implements IPreloadHandler {

	Patient patient;
	
	public ReminderPreloadHandler(Patient currentPatient) {
		patient = currentPatient;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePostProcess(org.javarosa.core.model.IFormDataModel, org.javarosa.core.model.IDataReference, java.lang.String)
	 */
	public boolean handlePostProcess(TreeElement node, String params) {
		ReminderRMSUtility reminderRms = (ReminderRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(ReminderRMSUtility.getUtilityName());
		DateData dateData = (DateData)node.getValue();
		if(dateData != null) {
			Date date = (Date)dateData.getValue();
			String patientName = patient.getName();
			int patientId = patient.getRecordId();
			Reminder reminder = new Reminder(date,patientId,patientName, "Follow up with " + patientName, "Follow up");
			reminderRms.writeToRMS(reminder);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePreload(java.lang.String)
	 */
	public IAnswerData handlePreload(String preloadParams) {
		//Null is our flag about whether we want a reminder for right now. We'll possibly
		//deal with this more robustly in the future
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "followup";
	}

}
