package org.javarosa.reminders;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IDaemon;
import org.javarosa.core.api.IModule;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.reminders.properties.ReminderPropertyRules;
import org.javarosa.reminders.storage.ReminderRMSUtility;
import org.javarosa.reminders.util.ReminderNotifierDaemon;

public class RemindersModule implements IModule {
	
	public void registerModule(Context context) {
		ReminderRMSUtility reminderRms = new ReminderRMSUtility(ReminderRMSUtility.getUtilityName());
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(reminderRms);
		
		IDaemon newDaemon = new ReminderNotifierDaemon();
		JavaRosaServiceProvider.instance().registerDaemon(newDaemon, ReminderNotifierDaemon.DEFAULT_NAME);
		
		PropertyManager propManager = JavaRosaServiceProvider.instance().getPropertyManager();
		propManager.addRules(new ReminderPropertyRules());
		PropertyUtils.initializeProperty(ReminderPropertyRules.REMINDERS_ENABLED_PROPERTY, ReminderPropertyRules.REMINDERS_ENABLED);
		
		String enabled = propManager.getSingularProperty(ReminderPropertyRules.REMINDERS_ENABLED_PROPERTY);
		if(ReminderPropertyRules.REMINDERS_ENABLED.equals(enabled)) {
			newDaemon.start();
		}
	}
}
