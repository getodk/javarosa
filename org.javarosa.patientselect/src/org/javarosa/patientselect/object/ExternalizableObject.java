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
	
	private Form mForm, sForm, resForm;
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
	
	/*
	 * Method to Initialize the patient registration Form
	 */
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
		
		//parent.setDisplay(this, mForm);l
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(mForm);
	}
	
	/*
	 * Method to Initialize the search Form
	 */
	public  void initPatientSearchForm() {

		sForm = new Form("Enter patient details to search");
		
		mPatientCode = new TextField("Patient Code", null, 32, 0);
		mPatientField = new TextField("Patient Name", null, 32, 0);
		
		sForm.append(mPatientCode);
		sForm.append(mPatientField);
		
		sForm.addCommand(new Command("Back", Command.BACK, 0));
		sForm.addCommand(search);
		
		sForm.setCommandListener(this);
		
		//parent.setDisplay(this, sForm);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(sForm);
		
		
	}
	
	/*
	 * Method to initialize the search results form to display results
	 */
	public void InitSearchResultsForm(Object resObject){
		
		resForm = new Form("Search Results");
		resForm.addCommand(new Command("Back", Command.BACK,0));
		
		resForm.setCommandListener(this);
		//resForm.append(resObject);
		
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(resForm);
	}
	
	/*
	 * Method to return the string on the data fields on the form
	 */
	public Object getPatientData(){
			
			patientData.append(mPatientField == null  ? "" : mPatientField.getString().trim());
			patientData.append(mPatientCode == null ? "" : mPatientCode.getString().trim() );
			patientData.append(mPatientSickness == null ? "" : mPatientSickness.getString().trim());
			patientData.append(mPatientTreatment == null ? "" : mPatientTreatment.getString().trim());
			patientData.append(mPatientSex == null ? "" : mPatientSex.getString().trim());
			patientData.append(mPatientLocation == null ? "" : mPatientLocation.getString().trim());
			patientData.append(mPeriod == null ? "" : mPeriod.getString().trim());
			
			//Date values... Report Date and Next visit Date
			patientData.append(mReportDate == null ? "" : mReportDate.getDate().toString().trim());
			patientData.append(mNextDateVisit == null ? "" : mNextDateVisit.getDate().toString().trim());
		
			return patientData;
	}
	
	/*
	 * Property to return the selected command[save]
	 */
	public Command getSaveCommand(){
		
		return save;
	}
	
	/*
	 * Property to return the selected command[search]
	 */
	public Command getSearchCommand(){
		return search;
	}
	
	/*
	 * Property to return the patientCode
	 */
	public String getPatientCode(){
		
		return mPatientCode.getString();
	}
	
	/*
	 * Property to return the patient Name
	 */
	public  String getPatientName(){
		
		return mPatientField.getString();
	}

	public void commandAction(Command c, Displayable d) {
		
		if(c == save){
			
			System.out.println("Entering Save Mode in [Externalizable]");
			
			Object patData = getPatientData();
			store.saveData(patData);
			
			System.out.println("Exiting Save Mode in [Externalizable]");
		}
		else if (c == search){
			
			System.out.println("Entering Search Mode in [Externalizable]");
			
			Object searchData = getPatientCode();
			
			Object returnedData = store.retrieveDataIndex(searchData);
			
			InitSearchResultsForm(returnedData);
			
			System.out.println("Exiting Search Mode in [Externalizable]");
		}
		else if(c.getCommandType() == Command.BACK){
			
			System.out.println("Back to list huh");

		}
		
	}

}
