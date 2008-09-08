package org.javarosa.patient.test;

import java.util.Vector;

//import org.javarosa.entitymgr.model.EntityData;
//import org.javarosa.entitymgr.model.EntityField;
//import org.javarosa.entitymgr.model.EntityFieldList;
//import org.javarosa.entitymgr.model.EntityFieldValue;
//import org.javarosa.entitymgr.model.EntityFieldValueList;
//import org.javarosa.entitymgr.model.EntityList;
//import org.javarosa.patientmgr.model.Patient;

public class PatientDataTest {

	//commented out until entitymgr builds
//	public static EntityData getTestPatientData(){
//		
//		//create patients.
//		Patient patient = new Patient(new Integer(10),"U/010","Mark","Kasode");
//		Vector patients = new Vector();
//		patients.addElement(new Patient(new Integer(1),"U/001","Clayton","Sims"));
//		patients.addElement(new Patient(new Integer(2),"U/002","Drew","Roos"));
//		patients.addElement(new Patient(new Integer(3),"U/003","Bruce","Macleod"));
//		patients.addElement(new Patient(new Integer(4),"U/004","Tom","Smyth"));
//		patients.addElement(new Patient(new Integer(5),"U/005","Brian","DeRenzi"));
//		patients.addElement(new Patient(new Integer(6),"U/006","Kieran","Sharper","schafer"));
//		patients.addElement(new Patient(new Integer(7),"U/007","Neal","Lesh"));
//		patients.addElement(new Patient(new Integer(8),"U/008","Muwanga","Simon","Peter"));
//		patients.addElement(new Patient(new Integer(9),"U/009","Frank","Jr","Nkuyahaga"));
//		patients.addElement(patient);
//		
//		EntityList entities = new EntityList(patients,new Patient().getClass());
//		
//		
//		//create patient fields.
//		Vector patientFields = new Vector();
//		patientFields.addElement(new EntityField(1,"last_visit_date"));
//		patientFields.addElement(new EntityField(2,"scheduled_visit_date"));
//		patientFields.addElement(new EntityField(3,"last_weight"));
//		patientFields.addElement(new EntityField(4,"last_height"));
//		patientFields.addElement(new EntityField(5,"who_stage"));
//		
//		EntityFieldList fields = new EntityFieldList(patientFields);
//		
//		
//		//create patient field values.
//		Vector patientFieldValues = new Vector();
//		patientFields.addElement(new EntityFieldValue(1,10,"01/01/2007"));
//		patientFields.addElement(new EntityFieldValue(2,10,"01/01/2008"));
//		patientFields.addElement(new EntityFieldValue(3,10,"67 Kg"));
//		patientFields.addElement(new EntityFieldValue(4,10,"5 inches"));
//		patientFields.addElement(new EntityFieldValue(5,10,"III"));
//		
//		EntityFieldValueList fieldValues = new EntityFieldValueList(patientFieldValues);
//		
//
//		return new EntityData(entities, fields, fieldValues,new Patient().getClass());
//	}
}
