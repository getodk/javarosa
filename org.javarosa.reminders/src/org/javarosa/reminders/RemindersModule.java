package org.javarosa.reminders;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.reminders.storage.ReminderRMSUtility;

public class RemindersModule implements IModule {
	
	public void registerModule(Context context) {
		ReminderRMSUtility reminderRms = new ReminderRMSUtility(ReminderRMSUtility.getUtilityName());
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(reminderRms);
	}

}
