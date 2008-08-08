package org.javarosa.reminders.view;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import org.javarosa.reminders.model.Reminder;

public class DisplayReminders extends Form {
	
	public StringItem head;
	public ChoiceGroup reminders;
	
	public static final Command DISMISS = new Command("Dismiss", Command.SCREEN, 1);
	public static final Command VIEW = new Command("View", Command.SCREEN, 1);
	
	public DisplayReminders() {
		super("Reminder");
		head = new StringItem("","The following reminders are ready");
		this.addCommand(DISMISS);
		this.addCommand(VIEW);
	}
	
	public void setReminders(Vector reminders) {
		this.deleteAll();
		this.append(head);
		
		this.reminders = new ChoiceGroup("", Choice.EXCLUSIVE);
		Enumeration en = reminders.elements();
		while(en.hasMoreElements()) {
			Reminder reminder = (Reminder)en.nextElement();
			this.reminders.append(reminder.getTitle(), null);
		}
		this.append(this.reminders);
	}
	
	public int getIndex() {
		return reminders.getSelectedIndex();
	}
}
