package org.javarosa.patientselect.object;

import javax.microedition.lcdui.*;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.patientselect.store.PatientListMetaData;
import org.javarosa.patientselect.store.PatientStore;

/**
* This encapsulates the Externalizable object to be passed to the RMS
* 
* 
* @author Mark Gerard
*
*/
public class ExternalizableObject extends PatientListMetaData implements CommandListener {

	static PatientStore store;
	static String storeName = "patientRecordStore";
	private Form mForm, sForm;
	private Command search, save;
	private StringBuffer patientData;
	private DateField mReportDate,mNextDateVisit;
	private TextField mPatientField, mPatientCode, mPatientLocation, mPatientSickness, mPatientTreatment, mPeriod, mPatientSex;
	
	public ExternalizableObject(String title) {
		
		store = new PatientStore(storeName);
		mForm = new Form(title);
		
		patientData = new StringBuffer();
		
		search = new Command("Search", Command.ITEM, 0);
		save = new Command("Save", Command.ITEM, 1);
	}
	
	public ExternalizableObject() {
		// TODO Auto-generated constructor stub
	}
	
	
	public  void initPatientRegistrationForm() {
		
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
		mForm.addCommand(save);
		
		mForm.setCommandListener(this);
		
		//parent.setDisplay(this, mForm);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(mForm);
	}
	
	public Object getPatientData(){
		
		if(validateData()){
			
			patientData.append(mPatientField.getString().trim());
			patientData.append(mPatientCode.getString().trim());
			patientData.append(mPatientSickness.getString().trim());
			patientData.append(mPatientTreatment.getString().trim());
			patientData.append(mPatientSex.getString().trim());
			patientData.append(mPatientLocation.getString().trim());
			patientData.append(mPeriod.getString().trim());
			
			patientData.append(mReportDate.getDate().toString().trim());
			patientData.append(mNextDateVisit.getDate().toString().trim());
		}
		return patientData;
	}
	public  void initPatientSearchForm() {

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

	public  boolean validateData() {
		
		boolean validated = false;
		
		if(mPatientField.getString().trim().length() != 0){
			
			mPatientField.getString();
			validated = true;
		}
		else{
			validated = false;
		}
		
		if(mPatientCode.getString().trim().length() != 0){
			
			mPatientCode.getString();
			validated = true;
		}
		
		else{
			
			validated = false;
		}
		
		if(mPatientLocation.getString().trim().length() != 0){
			
			mPatientLocation.getString();
			validated = true;
		}
		else{
			validated = false;
		}
		
		if(mPatientSickness.getString().trim().length() != 0){
			
			mPatientSickness.getString();
			validated = true;
		}
		else{
			validated = false;
		}
		
		if(mPatientTreatment.getString().trim().length() != 0){
			
			mPatientTreatment.getString();
			validated = true;
		}
		else{
			validated = false;
		}
		
		if(mPeriod.getString().trim().length() != 0){
			
			mPeriod.getString();
			validated = true;
		}
		else{
			validated = false;
		}
		
		if(mPatientSex.getString().trim().length() != 0){
			
			mPatientSex.getString();
			validated = true;
		}
		else{
			validated = false;
		}
		
		if(mNextDateVisit.getDate().toString().trim().length() != 0){
			
			mNextDateVisit.getDate().toString();
			validated = true;
		}
		else{
			validated = false;
		}
		
		if(mReportDate.getDate().toString().trim().length() != 0){
			
			mReportDate.getDate().toString();
			validated = true;
		}
		else{
			
			validated = false;
		}
		
		return validated;
	}
	
	public Command getCommandType(){
		
		return save;
	}

	public void commandAction(Command c, Displayable d) {
		
		if(c == save){
			System.out.println("In");
			
			PatientListMetaData metaDataObject = new PatientListMetaData();
			System.out.println("In");
			
			Object patData = getPatientData();
			
			System.out.println("In");
			store.writeToRMS(patData, metaDataObject);
			
			System.out.println("out");
		}
		else if (c == search){
			
		}
		
	}

}
