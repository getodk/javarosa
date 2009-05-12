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
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.reminders.model.Reminder;
import org.javarosa.reminders.storage.ReminderRMSUtility;
import org.javarosa.reminders.util.ReminderListContext;
import org.javarosa.reminders.view.DisplayReminders;
import org.javarosa.reminders.view.HandleReminder;

/**
 * @author Clayton Sims
 *
 */
public class DisplayReminderActivity implements IActivity, CommandListener {
	
	public static final Command VIEW_REMINDERS = new Command("View Reminders", Command.SCREEN, 3);
	
	private static final Command DONE_VIEWING = new Command("Done", Command.SCREEN, 1);

	/** Reminder */
	Vector reminders;
	
	/** Reminder */
	Vector changedReminders;
	
	/** Reminder */
	Vector removedReminders;
	
	IShell parent;
	
	DisplayReminders remindersDisplay;
	
	HandleReminder handleReminder;
	
	ReminderListContext context;
	
	int viewMode;
	
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
		if(context instanceof ReminderListContext) {
			this.context = (ReminderListContext)context;
			viewMode = this.context.getViewMode();
		}
		changedReminders = new Vector();
		removedReminders = new Vector();
		if(viewMode == ReminderListContext.VIEW_ALL) {
			remindersDisplay.addCommand(DONE_VIEWING);
		}
		returnToList();
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command command, Displayable display) {
		if(display == remindersDisplay) {
			Reminder selectedReminder = remindersDisplay.getReminder(command);
			if(command.getLabel().equals(DisplayReminders.DISMISS_STRING)) {
				selectedReminder.setNotified(true);
				changedReminders.addElement(selectedReminder);
				reminders.removeElement(selectedReminder);
				returnToList();
			}
			if(command.getLabel().equals(DisplayReminders.VIEW_STRING)) { 
				handleReminder = new HandleReminder(selectedReminder,(viewMode == ReminderListContext.VIEW_TRIGGERED));
				handleReminder.setCommandListener(this);
				parent.setDisplay(this, handleReminder);
			}
			if(command == DONE_VIEWING) {
				commitChanges();
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
			}
		}
		if(display == handleReminder) {
			if(command == HandleReminder.DONE) {
				Reminder selectedReminder = handleReminder.getReminder();
				if(viewMode == ReminderListContext.VIEW_TRIGGERED) {
					reminders.removeElement(selectedReminder);
					selectedReminder.setNotified(true);
				}
				changedReminders.addElement(selectedReminder);
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
				if(viewMode == ReminderListContext.VIEW_TRIGGERED) {
					reminders.removeElement(selectedReminder);
					selectedReminder.setNotified(true);
				}
				changedReminders.addElement(selectedReminder);
				returnToList();
			}
		}
	}
	
	private void returnToList() {
		if(reminders.size() > 0) {
			remindersDisplay.setReminders(reminders, viewMode);
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
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
