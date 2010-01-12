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

package org.javarosa.reminders.thread;

import java.util.Date;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

import org.javarosa.reminders.model.Reminder;
import org.javarosa.reminders.util.ReminderNotifierDaemon;

public class ReminderBackgroundService extends TimerTask {
	private ReminderNotifierDaemon notifier;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Vector reminders = notifier.getReminders();
		Enumeration en = reminders.elements();
		Date current = new Date();
		Vector newExpiredReminders = new Vector();
		while(en.hasMoreElements()) {
			Reminder reminder = (Reminder)en.nextElement();
			Date followUpDate = reminder.getFollowUpDate();
			if(followUpDate.getTime() <= current.getTime()) {
				if(!reminder.isNotified()) {
					newExpiredReminders.addElement(reminder);
				}
			}
		}
		if(newExpiredReminders.size() > 0 ) {
			notifier.remindersExpired(newExpiredReminders);
		}
	}
	
	
	public void setReminderNotifier(ReminderNotifierDaemon notifier) {
		this.notifier = notifier;
	}
}
