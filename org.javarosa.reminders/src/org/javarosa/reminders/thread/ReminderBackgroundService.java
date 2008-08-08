package org.javarosa.reminders.thread;

import java.util.Date;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

import org.javarosa.reminders.model.Reminder;
import org.javarosa.reminders.util.ReminderNotifier;

public class ReminderBackgroundService extends TimerTask {
	private ReminderNotifier notifier;
	
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
	
	
	public void setReminderNotifier(ReminderNotifier notifier) {
		this.notifier = notifier;
	}
}
