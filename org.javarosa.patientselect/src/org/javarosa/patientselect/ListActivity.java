package org.javarosa.patientselect;

import java.util.Vector;
import java.io.IOException;
import javax.microedition.lcdui.*;

import org.javarosa.core.Context;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.JavaRosaServiceProvider;
import javax.microedition.rms.InvalidRecordIDException;
import org.javarosa.core.services.storage.utilities.MetaDataObject;

/**
* This IActivity encapsulates a discrete execution unit, with the
* ability to List patients and allow to select a particular patient.
* 
* The IActivity also release control flow from the IShell
* 
* @author Mark Gerard
*
*/
public class ListActivity implements IActivity, CommandListener {
	
private static String pName, pCode, pLocation, pSickness, pDiagnosis, pPeriod, pSex, pReportDate, pNextVisitDate;
	
	private PatientStore store;
	
	private Form mForm, sForm;
	private List list;
	private Command search;
	private Alert alert;
	private Gauge patientIndicator;
	
	private TextField mPatientField, mPatientCode, mPatientLocation, mPatientSickness, mPatientTreatment, mPeriod, mPatientSex;
	private DateField mReportDate,mNextDateVisit;
	
	private String storeName = "patientRecordStore";
	private IShell parent = null;
	private Context context;
	
	public ListActivity(IShell p, String title) {
		
		parent = p;
		store = new PatientStore(storeName, 0);
		this.mForm = new Form(title);
	}
		
	public void commandAction(Command c, Displayable s) {
			
			if (c.getCommandType() == Command.EXIT) {
				
				parent.exitShell();
			
			}
			else if(c.getCommandType() == Command.SCREEN){
				
				int choiceId = list.getSelectedIndex();
				
				if(choiceId == 0){
					
					System.out.println(choiceId);
					showSearchPatientForm();
				}
				
				else if(choiceId == 1){
					
					System.out.println(choiceId);
					showRegisterPatientForm();
				}
			}
			
			else if(c.getCommandType() == Command.BACK){
				
				parent.setDisplay(this, list);
				JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
			}
			
			else if(c == search){
				
				try {
					searchPatient();
					
				} catch (InvalidRecordIDException e) {
					
					e.printStackTrace();
					
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			else if(c.getCommandType() == Command.ITEM){
				
				System.out.println("Entering save mode");
				
				if (validateData()){
					
					savePatientData();
				}
				else{
					
					showAlert("Save Error!","Data not saved!");
				}
			
			System.out.println("Exiting save mode [Data saved]");
		}
	}

	private void searchPatient() throws InvalidRecordIDException, IOException {
		
		System.out.println("Entering search function");
		
		pName = mPatientField.getString();
		pCode = mPatientCode.getString();
		
		if(pCode.length() > 0){
			
			Object formData = null;
			
			int recordId = 0;
			formData = store.retrieveByteDataFromRMS(recordId);
			
			String data = formData.toString();
			
			System.out.println(data);
			
			Form resultsForm = new Form("Search Results");
			
			resultsForm.addCommand(new Command("Back", Command.BACK, 1));
			resultsForm.append(data);
			
			parent.setDisplay(this, resultsForm);
			JavaRosaServiceProvider.instance().getDisplay().setCurrent(resultsForm);
		}
		else if (pCode.length() < 0 && pName.length() < 0){
			
			showAlert("Search Error!","Enter atleast Patient Name or Patient Code to proceed with search");
		}
		
		System.out.println("Exiting search function");
	}

	private boolean validateData() {
		
		boolean validated = false;
		
		if(mPatientField.getString().trim().length() != 0){
			
			pName = mPatientField.getString();
			validated = true;
		}
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientCode.getString().trim().length() != 0){
			
			pCode = mPatientCode.getString();
			validated = true;
		}
		
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientLocation.getString().trim().length() != 0){
			
			pLocation = mPatientLocation.getString();
			validated = true;
		}
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientSickness.getString().trim().length() != 0){
			
			pSickness = mPatientSickness.getString();
			validated = true;
		}
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientTreatment.getString().trim().length() != 0){
			
			pDiagnosis = mPatientTreatment.getString();
			validated = true;
		}
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPeriod.getString().trim().length() != 0){
			
			pPeriod = mPeriod.getString();
			validated = true;
		}
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mPatientSex.getString().trim().length() != 0){
			
			pSex = mPatientSex.getString();
			validated = true;
		}
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mNextDateVisit.getDate().toString().trim().length() != 0){
			
			pNextVisitDate = mNextDateVisit.getDate().toString();
			validated = true;
		}
		else{
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		if(mReportDate.getDate().toString().trim().length() != 0){
			
			pReportDate = mReportDate.getDate().toString();
			validated = true;
		}
		else{
			
			showAlert("Patient Data","You did not enter all the required patient Data! Save cannot proceed");
			validated = false;
		}
		
		return validated;
	}

	private void savePatientData() {
		
		String[] patientData = {pName, pLocation, pSickness, pDiagnosis, pPeriod, pNextVisitDate, pReportDate, pSex};
		
		Object patData = patientData;
		Object metaDataObject = null;
		
		if(patData != null){
			
			store.writeToRMS(patData, (MetaDataObject) metaDataObject);
		}
	}

	private void showList() {
		
		list = new List("Select an option to continue", Choice.IMPLICIT);
		
		list.append("Search Patient", null);
		list.append("Enter New Patient", null);
		
		list.addCommand(new Command("Proceed", Command.SCREEN, 0));
		list.addCommand(new Command("Exit", Command.EXIT, 1));
		
		list.setCommandListener(this);
		
		parent.setDisplay(this, list);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
	}
	
	private void showRegisterPatientForm() {
		
		mForm = new Form("Enter New Patient Details");
		
		mPatientField = new TextField("Patient Name",null, 32, 0);
		mPatientCode = new TextField("Patient Code",null, 32, 0);
		mPatientSickness = new TextField("Diagnosis", null, 32, 0);
		mPatientTreatment = new TextField("Treatment Administered/Recommended", null , 32 ,0);
		mPatientSex = new TextField("Patient sex", null, 32, 0);
		mPatientLocation = new TextField("Location", null, 32, 0);
		mPeriod = new TextField("Report For Period", null, 32 , 0);
		
		mReportDate = new DateField("Report Date", DateField.DATE_TIME);
		mNextDateVisit = new DateField("Next Visit Date",DateField.DATE);
		
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
		mForm.addCommand(new Command("Save Patient", Command.ITEM, 0));
		
		mForm.setCommandListener(this);
		
		parent.setDisplay(this, mForm);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(mForm);
	}
	
	private void showSearchPatientForm() {
		
		search = new Command("Search", Command.ITEM, 3);
		
		sForm = new Form("Enter patient details to search");
		
		mPatientCode = new TextField("Patient Code", null, 32, 0);
		mPatientField = new TextField("Patient Name", null, 32, 0);
		
		sForm.append(mPatientCode);
		sForm.append(mPatientField);
		
		sForm.addCommand(new Command("Back", Command.BACK, 0));
		sForm.addCommand(search);
		
		sForm.setCommandListener(this);
		
		parent.setDisplay(this, sForm);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(sForm);
		
		
	}

	private boolean showAlert(String message, String secMessage) {
		
		alert = new Alert(message, secMessage, null, null);
		
		alert.setTimeout(5000);
		
		patientIndicator = new Gauge(null, false,Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
		
		alert.setIndicator(patientIndicator);
		
		parent.setDisplay(this, alert);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(alert);
		
		return false;
	}

	public void showAppMessage() {
		// TODO Auto-generated method stub
		
	}

	public void contextChanged(Context globalContext) {

		Vector contextChanges = this.context.mergeInContext(context);
		contextChanges.capacity();
		
	}

	public void halt() {
		// TODO Auto-generated method stub
		
	}

	public void resume(Context globalContext) {

		this.contextChanged(context);
		JavaRosaServiceProvider.instance().showView(this.list);
		
	}

	public void start(Context context) {
		
		showList();
		
		this.context = context;
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(list);
	}

	public void destroy() {
		
	}
	
	public void searchByBoth(String patientName, String patientCode) {
		// TODO Auto-generated method stub
		
	}

	public String searchByCode(String code) {
		// TODO Auto-generated method stub
		return null;
	}

	public String searchByName(String patientName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String selectPatient(String patientId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
