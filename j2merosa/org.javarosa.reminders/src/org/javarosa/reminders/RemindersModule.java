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

package org.javarosa.reminders;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IDaemon;
import org.javarosa.core.api.IModule;
import org.javarosa.core.services.IPropertyManager;
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
		
		IPropertyManager propManager = JavaRosaServiceProvider.instance().getPropertyManager();
		propManager.addRules(new ReminderPropertyRules());
		PropertyUtils.initializeProperty(ReminderPropertyRules.REMINDERS_ENABLED_PROPERTY, ReminderPropertyRules.REMINDERS_ENABLED);
		
		String enabled = propManager.getSingularProperty(ReminderPropertyRules.REMINDERS_ENABLED_PROPERTY);
		if(ReminderPropertyRules.REMINDERS_ENABLED.equals(enabled)) {
			newDaemon.start();
		}
	}
}
