package org.javarosa.reminders.util;

import java.io.IOException;
import java.util.Timer;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.reminders.storage.ReminderRMSUtility;
import org.javarosa.reminders.thread.ReminderBackgroundService;

public class ReminderNotifier {
	public static final int period = 1000;// * 60 * 5;

	INotificationReceiver notificationReceiver;

	Timer timer;
	boolean running = false;

	public void startService() {
		if (!running) {
			timer = new Timer();
			ReminderBackgroundService service = new ReminderBackgroundService();
			service.setReminderNotifier(this);
			timer.schedule(service, 0, period);
			running = true;
		}
	}

	public void stopService() {
		if (running) {
			timer.cancel();
			running = false;
		}
	}

	/**
	 * @return the reminders
	 */
	public Vector getReminders() {
		ReminderRMSUtility reminderRms = (ReminderRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(ReminderRMSUtility.getUtilityName());
		try {
			return reminderRms.getReminders();
		} catch (IOException e) {
			this.stopService();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			this.stopService();
			e.printStackTrace();
		} catch (InstantiationException e) {
			this.stopService();
			e.printStackTrace();
		} catch (UnavailableExternalizerException e) {
			this.stopService();
			e.printStackTrace();
		}
		return new Vector();
	}

	/**
	 * @return the notificationReceiver
	 */
	public INotificationReceiver getNotificationReceiver() {
		return notificationReceiver;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * @param notificationReceiver
	 *            the notificationReceiver to set
	 */
	public void setNotificationReceiver(
			INotificationReceiver notificationReceiver) {
		this.notificationReceiver = notificationReceiver;
	}

	public void remindersExpired(Vector expiredReminders) {
		if (notificationReceiver != null) {
			notificationReceiver.receiveReminders(expiredReminders);
		}
	}

}
