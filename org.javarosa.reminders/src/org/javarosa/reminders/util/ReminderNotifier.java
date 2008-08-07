package org.javarosa.reminders.util;

import java.util.Timer;
import java.util.Vector;

import org.javarosa.reminders.thread.ReminderBackgroundService;

public class ReminderNotifier {
	public static final int period = 1000*60*5;
	
	INotificationReceiver notificationReceiver;

	/** Reminder */
	Vector reminders;
	
	Timer timer;
	
	public void startService() {
		ReminderBackgroundService service = new ReminderBackgroundService();
		service.setReminderNotifier(this);
		timer.schedule(service, 0, period);
	}
	
	public void stopService() {
		timer.cancel();
	}

	/**
	 * @return the reminders
	 */
	public Vector getReminders() {
		return reminders;
	}
	
	/**
	 * @return the notificationReceiver
	 */
	public INotificationReceiver getNotificationReceiver() {
		return notificationReceiver;
	}

	/**
	 * @param notificationReceiver the notificationReceiver to set
	 */
	public void setNotificationReceiver(INotificationReceiver notificationReceiver) {
		this.notificationReceiver = notificationReceiver;
	}

	/**
	 * @param reminders the reminders to set
	 */
	public void setReminders(Vector reminders) {
		this.reminders = reminders;
	}
	
	public void remindersExpired(Vector expiredReminders) {
		if(notificationReceiver != null) {
			notificationReceiver.receiveReminders(expiredReminders);
		}
	}
	
	
}
