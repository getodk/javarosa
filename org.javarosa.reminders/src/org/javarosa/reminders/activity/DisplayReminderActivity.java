/**
 * 
 */
package org.javarosa.reminders.activity;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.reminders.model.Reminder;
import org.javarosa.reminders.storage.ReminderRMSUtility;
import org.javarosa.reminders.view.DisplayReminders;
import org.javarosa.reminders.view.HandleReminder;

/**
 * @author Clayton Sims
 *
 */
public class DisplayReminderActivity implements IActivity, CommandListener {

	/** Reminder */
	Vector reminders;
	
	Vector changedReminders;
	
	Vector removedReminders;
	
	IShell parent;
	
	DisplayReminders remindersDisplay;
	
	HandleReminder handleReminder;
	
	Context context;
	
	public DisplayReminderActivity(IShell parent) {
		this.parent = parent;
		remindersDisplay = new DisplayReminders();
		remindersDisplay.setCommandListener(this);
	}
	
	public void setReminders(Vector reminders) {
		this.reminders = reminders;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = context;
		changedReminders = new Vector();
		removedReminders = new Vector();
		
		returnToList();
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command command, Displayable display) {
		if(display == remindersDisplay) {
			int index = remindersDisplay.getIndex();
			Reminder selectedReminder = (Reminder)reminders.elementAt(index);
			if(command == DisplayReminders.DISMISS) {
				selectedReminder.setNotified(true);
				changedReminders.addElement(selectedReminder);
				reminders.removeElement(selectedReminder);
				returnToList();
			}
			if(command == DisplayReminders.HANDLE) { 
				handleReminder = new HandleReminder(selectedReminder);
				handleReminder.setCommandListener(this);
				parent.setDisplay(this, handleReminder);
			}
		}
		if(display == handleReminder) {
			if(command == HandleReminder.DONE) {
				Reminder selectedReminder = handleReminder.getReminder();
				selectedReminder.setNotified(true);
				changedReminders.addElement(selectedReminder);
				reminders.removeElement(selectedReminder);
				returnToList();
			}
			if(command == HandleReminder.REMOVE) {
				Reminder selectedReminder = handleReminder.getReminder();
				removedReminders.addElement(selectedReminder);
				reminders.removeElement(selectedReminder);
				returnToList();
			}
			if(command == HandleReminder.UPDATE) {
				Reminder selectedReminder = handleReminder.getUpdatedReminder();
				selectedReminder.setNotified(true);
				changedReminders.addElement(selectedReminder);
				reminders.removeElement(selectedReminder);
				returnToList();	
			}
		}
	}
	
	private void returnToList() {
		if(reminders.size() > 0) {
			remindersDisplay.setReminders(reminders);
			parent.setDisplay(this, remindersDisplay);
		}
		else {
			commitChanges();
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
		}
	}
	
	private void commitChanges() {
		ReminderRMSUtility utility = (ReminderRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(ReminderRMSUtility.getUtilityName());
		Enumeration en = changedReminders.elements();
		while(en.hasMoreElements()) {
			Reminder reminder = (Reminder)en.nextElement();
			utility.updateReminder(reminder);
		}
		//I think we need to sort these largest to smallest so that the RMS won't rearrange Id's.
		en = sortLargestToSmallestById(removedReminders).elements();
		while(en.hasMoreElements()) {
			Reminder reminder = (Reminder)en.nextElement();
			utility.removeReminder(reminder);
		}
	}
	
	private Vector sortLargestToSmallestById(Vector reminders) {
		Vector sortedVector = new Vector();
		Enumeration en = reminders.elements();
		while(en.hasMoreElements()) {
			Reminder reminder = (Reminder)en.nextElement();
			int newIndex = 0;
			for(int i = 0 ; i < sortedVector.size() ; ++i ) {
				Reminder innerReminder = (Reminder)sortedVector.elementAt(i);
				if(innerReminder.getRecordId() < reminder.getRecordId()) {
					newIndex = i;
				}
			}
			sortedVector.insertElementAt(reminder, newIndex);
		}
		return sortedVector;
	}
}
