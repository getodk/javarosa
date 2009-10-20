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

package org.javarosa.reminders.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.api.IView;
import org.javarosa.reminders.model.Reminder;

public class HandleReminder extends Form implements IView{

	Reminder reminder;
	
	TextField title; 
	
	TextField reminderMessage; 
	
	public static final Command UPDATE = new Command("Update", Command.SCREEN, 1);
	public static final Command REMOVE = new Command("Delete Reminder", Command.SCREEN, 2);
	public static final Command DONE = new Command("Done", Command.SCREEN, 1);
	
	public HandleReminder(Reminder reminder, boolean readOnly) {
		super("Followup Reminder");
		
		
		this.reminder = reminder;
		title = new TextField("Title: ", reminder.getTitle(), 50, TextField.ANY);
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
	public Object getScreenObject() {
		return this;
	}
}
