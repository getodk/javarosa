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

package org.javarosa.patient.activity.view;

import javax.microedition.lcdui.Command;

import org.javarosa.core.api.IView;
import org.javarosa.patient.model.Patient;

import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.DateField;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;

public class PatientEditView extends FramedForm implements IView {

	public static final Command DONE = new Command("Done",Command.SCREEN, 1);
	public static final Command CANCEL = new Command("Cancel",Command.CANCEL, 1);
	
	Patient savedPatient;
	
	TextField familyName;
	TextField givenName;
	TextField patientId;
	//TextField age;
	
	DateField date;
	
	DateField treatmentDate;
	
	ChoiceGroup gender;
	
	
	public PatientEditView(String title) {
		super(title);
		givenName = new TextField("Given Name", "",50,TextField.ANY);
		familyName = new TextField("Family Name", "",50,TextField.ANY);
		patientId = new TextField("Patient ID", "",50,TextField.ANY);
		//age = new TextField("Age", "", 50, TextField.DECIMAL);
		date = new DateField("Date of Birth", DateField.DATE);
		treatmentDate = new DateField("Start Date of Treatment", DateField.DATE);
		
		gender = new ChoiceGroup("Gender",ChoiceGroup.EXCLUSIVE);
		gender.append(new ChoiceItem("Male",null,Choice.EXCLUSIVE));
		gender.append(new ChoiceItem("Female",null,Choice.EXCLUSIVE));
		gender.append(new ChoiceItem("Unkown",null,Choice.EXCLUSIVE));
		
		this.append(givenName);
		this.append(familyName);
		this.append(patientId);
		//this.append(age);
		this.append(date);
		this.append(treatmentDate);
		this.append(gender);
		this.addCommand(DONE);
		this.addCommand(CANCEL);
	}
	
	public PatientEditView(String title, Style style) {
		super(title, style);
	}
	
	public Patient getPatient() {
		if(savedPatient == null) {
			savedPatient = new Patient();
		}
		savedPatient.setGivenName(givenName.getString());
		savedPatient.setFamilyName(familyName.getString());
		savedPatient.setPatientIdentifier(this.patientId.getString());
		if(gender.getSelectedIndex() == 0) {
			savedPatient.setGender(Patient.SEX_MALE);
		} else if(gender.getSelectedIndex() == 1) {
			savedPatient.setGender(Patient.SEX_FEMALE);
		}else if(gender.getSelectedIndex() == 2) {
			savedPatient.setGender(Patient.SEX_UNKNOWN);
		}
		savedPatient.setBirthDate(date.getDate());
		savedPatient.setTreatmentStartDate(treatmentDate.getDate());
		return savedPatient;
	}
	
	public void setPatient(Patient patient) {
		familyName.setText(patient.getFamilyName());
		givenName.setText(patient.getGivenName());
		patientId.setText(patient.getPatientIdentifier());
		
		date.setDate(patient.getBirthDate());
		
		treatmentDate.setDate(patient.getTreatmentStartDate());
				
		switch(patient.getGender()) {
		case(Patient.SEX_MALE):
			gender.setSelectedIndex(0, true);
			break;
		case(Patient.SEX_FEMALE):
			gender.setSelectedIndex(1, true);
			break;
		case(Patient.SEX_UNKNOWN):
			gender.setSelectedIndex(2, true);
			break;
		}
		this.savedPatient = patient;
	}

	public Object getScreenObject() {
		return this;
	}
}
