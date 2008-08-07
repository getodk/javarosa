package org.javarosa.reminders.util;

import java.util.Vector;

import org.javarosa.reminders.model.Reminder;

public interface INotificationReceiver {
	public void receiveReminder(Reminder reminder);
	public void receiveReminders(Vector reminders);
}
