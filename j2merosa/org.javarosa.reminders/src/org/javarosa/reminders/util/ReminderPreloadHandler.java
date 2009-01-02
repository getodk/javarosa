package org.javarosa.reminders.util;

import java.util.Date;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
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
			Integer patientIdValue = patient.getId();
			int patientId = -1;
			if(patientIdValue != null) {
				patientId = patientIdValue.intValue();
			}
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
