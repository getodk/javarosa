package org.javarosa.reminders.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import org.javarosa.reminders.model.Reminder;

public class HandleReminder extends Form {

	Reminder reminder;
	
	TextField title; 
	
	TextField reminderMessage; 
	
	public static final Command UPDATE = new Command("Update", Command.SCREEN, 1);
	public static final Command REMOVE = new Command("Delete Reminder", Command.SCREEN, 2);
	public static final Command DONE = new Command("Done", Command.SCREEN, 1);
	
	public HandleReminder(Reminder reminder, boolean readOnly) {
		super("Followup Reminder");
		
		
		this.reminder = reminder;
		title = new TextField("", reminder.getTitle(), 50, TextField.ANY);
		StringItem readOnlyTitle = new StringItem("", reminder.getTitle());
		StringItem patientText = new StringItem("Patient: ", reminder.getPatientName());
		StringItem dateItem = new StringItem("Date: ", reminder.getFollowUpDate().toString());
		
		reminderMessage = new TextField("Message: ", reminder.getText(),200, TextField.ANY);
		StringItem readOnlyMessage = new StringItem("Message: ", reminder.getText()); 
		
		if(!readOnly) {
			this.append(title);
		} else {
			this.append(readOnlyTitle);
		}
		this.append(dateItem);
		this.append(patientText);
		if(!readOnly) {
			this.append(reminderMessage);
		} else {
			this.append(readOnlyMessage);
		}
		
		if(!readOnly) {
			this.addCommand(UPDATE);
		}
		this.addCommand(REMOVE);
		this.addCommand(DONE);
	}
	
	public Reminder getReminder() {
		return reminder;
	}
	
	public Reminder getUpdatedReminder() {
		reminder.setText(reminderMessage.getString());
		reminder.setTitle(title.getString());
		return reminder;
	}
}
