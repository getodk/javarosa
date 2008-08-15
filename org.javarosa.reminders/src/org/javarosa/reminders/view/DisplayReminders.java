package org.javarosa.reminders.view;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.util.Map;
import org.javarosa.reminders.model.Reminder;
import org.javarosa.reminders.util.ReminderListContext;

public class DisplayReminders extends Form {
	
	public StringItem head;
	
	/** Reminder */
	Vector pending;
	
	/** Command -> Reminder */
	Map reminderMapping;
	
	public static final String DISMISS_STRING = "Dismiss";
	public static final String VIEW_STRING = "View";
	
	public DisplayReminders() {
		super("Reminder");
	}
	
	public void setReminders(Vector reminders, int viewMode) {
		reminderMapping = new Map();
		this.deleteAll();
		if(viewMode == ReminderListContext.VIEW_TRIGGERED) {
			this.setTitle("The following reminders are ready");
			//head = new StringItem("","The following reminders are ready");
		} else if(viewMode == ReminderListContext.VIEW_ALL){
			this.setTitle("Followup Reminders");
			//head = new StringItem("","Followup Reminders");
		}
		//this.append(head);
		
		if(viewMode == ReminderListContext.VIEW_ALL){
			//#style title
			this.append(new StringItem("","Expired"));
		}
		
		pending = new Vector();
		
		Enumeration en = reminders.elements();
		while(en.hasMoreElements()) {
			Reminder reminder = (Reminder)en.nextElement();
			if(viewMode == ReminderListContext.VIEW_TRIGGERED) {
				addReminder(reminder);
			} else if(viewMode == ReminderListContext.VIEW_ALL) {
				if(reminder.isNotified()) {
					addReminder(reminder);
				}
				else {
					pending.addElement(reminder);
				}
			}
		}
		if(viewMode == ReminderListContext.VIEW_ALL) {
			//#style title
			this.append(new StringItem("","Pending"));
		}
		en = pending.elements();
		while(en.hasMoreElements()) {
			addReminder((Reminder)en.nextElement());
		}
	}
	
	private void addReminder(Reminder reminder) {
		String text = reminder.getFollowUpDate().toString() + ": " + reminder.getTitle() + " with " + reminder.getPatientName();
		TextField field = new TextField("", text, 500, TextField.UNEDITABLE);
		Command viewCommand = new Command(VIEW_STRING, Command.SCREEN, 3);
		//Note that this is a 'cancel' event in order that it show up in the correct spot
		Command dismissCommand = new Command(DISMISS_STRING, Command.CANCEL, 1);
		
		reminderMapping.put(viewCommand, reminder);
		reminderMapping.put(dismissCommand, reminder);
		field.addCommand(viewCommand);
		field.addCommand(dismissCommand);
		this.append(field);
	}
	
	public Reminder getReminder(Command command) {
		return (Reminder)reminderMapping.get(command); 
	}
}
