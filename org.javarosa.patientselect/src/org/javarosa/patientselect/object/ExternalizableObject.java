package org.javarosa.patientselect.object;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.*;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.patientselect.store.PatientListMetaData;
import org.javarosa.patientselect.store.PatientStore;

/**
* This IActivity encapsulates a discrete execution unit, with the
* ability to List patients and allow to select a particular patient.
* 
* The IActivity also release control flow from the IShell
* 
* @author Mark Gerard
*
*/
public class ExternalizableObject extends PatientListMetaData implements Externalizable {

	static PatientStore store;
	static Form mForm, sForm;
	
	static TextField mPatientField, mPatientCode, mPatientLocation, mPatientSickness, mPatientTreatment, mPeriod, mPatientSex;
	static DateField mReportDate,mNextDateVisit;
	
	static String storeName = "patientRecordStore";

	
	public ExternalizableObject(String title) {
		
		store = new PatientStore(storeName, 0);
		mForm = new Form(title);
	}
	
	public static void initPatientRegistrationForm() {
		
		mForm = new Form("Enter New Patient Details");
		
		mPatientField = new TextField("Patient Name:",null, 32, 0);
		mPatientCode = new TextField("Patient Code:",null, 32, 0);
		mPatientSickness = new TextField("Diagnosis:", null, 32, 0);
		mPatientTreatment = new TextField("Treatment Administered/Recommended:", null , 32 ,0);
		mPatientSex = new TextField("Patient sex:", null, 32, 0);
		mPatientLocation = new TextField("Location:", null, 32, 0);
		mPeriod = new TextField("Report For Period:", null, 32 , 0);
		
		mReportDate = new DateField("Report Date:", DateField.DATE_TIME);
		mNextDateVisit = new DateField("Next Visit Date:",DateField.DATE);
		
		mReportDate.setDate(new java.util.Date());
		
		mForm.append(mPatientField);
		mForm.append(mPatientCode);
		mForm.append(mPatientSex);
		mForm.append(mPatientSickness);
		mForm.append(mPatientTreatment);
		mForm.append(mPatientLocation);
		mForm.append(mReportDate);
		mForm.append(mNextDateVisit);
		mForm.append(mPeriod);
		
		mForm.addCommand(new Command("Back", Command.BACK, 0));
		mForm.addCommand(new Command("Save Patient", Command.ITEM, 1));
		
		//mForm.setCommandListener(this);
		
		//parent.setDisplay(this, mForm);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(mForm);
	}
	
	public static void initPatientSearchForm() {

		sForm = new Form("Enter patient details to search");
		
		mPatientCode = new TextField("Patient Code", null, 32, 0);
		mPatientField = new TextField("Patient Name", null, 32, 0);
		
		sForm.append(mPatientCode);
		sForm.append(mPatientField);
		
		sForm.addCommand(new Command("Back", Command.BACK, 0));
		
		//sForm.setCommandListener(this);
		
		//parent.setDisplay(this, sForm);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(sForm);
		
		
	}

	public static boolean validateData() {
		
		boolean validated = false;
		
		if(mPatientField.getString().trim().length() != 0){
			
			mPatientField.getString();
			validated = true;
		}
		else{
			
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientCode.getString().trim().length() != 0){
			
			mPatientCode.getString();
			validated = true;
		}
		
		else{
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientLocation.getString().trim().length() != 0){
			
			mPatientLocation.getString();
			validated = true;
		}
		else{
			
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientSickness.getString().trim().length() != 0){
			
			mPatientSickness.getString();
			validated = true;
		}
		else{
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientTreatment.getString().trim().length() != 0){
			
			mPatientTreatment.getString();
			validated = true;
		}
		else{
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPeriod.getString().trim().length() != 0){
			
			mPeriod.getString();
			validated = true;
		}
		else{
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientSex.getString().trim().length() != 0){
			
			mPatientSex.getString();
			validated = true;
		}
		else{
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mNextDateVisit.getDate().toString().trim().length() != 0){
			
			mNextDateVisit.getDate().toString();
			validated = true;
		}
		else{
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mReportDate.getDate().toString().trim().length() != 0){
			
			mReportDate.getDate().toString();
			validated = true;
		}
		else{
			
			//showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		return validated;
	}

}
