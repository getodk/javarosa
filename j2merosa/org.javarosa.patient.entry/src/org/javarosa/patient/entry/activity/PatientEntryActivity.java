package org.javarosa.patient.entry.activity;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.model.data.NumericListData;
import org.javarosa.patient.storage.PatientRMSUtility;
import org.javarosa.patient.util.DateValueTuple;
import org.javarosa.xform.util.XFormUtils;

/**
 * NOTICE:
 * 
 * 
 * This class, and the entire org.javarosa.patient.entry Module all only
 * exist because <group> and <repeat> are not yet available in our XForms 
 * subset. 
 * 
 * 
 * Please do not take the code in the org.javarosa.patient.entry project
 * as an example of a proper use of the JavaRosa code base. It will be removed
 * as soon as it is possible to represent the logic in the PatientEntryForm in
 * our XForms subset.
 * 
 * @author Clayton Sims
 *
 */
public class PatientEntryActivity implements IActivity {

	public static final String PATIENT_ENTRY_FORM_KEY = "jr-patient-reg";
	public static final String NEW_PATIENT_ID = "patient-id";
	
	Context context;
	IShell parent;
	
	FormDef patientEntryForm;
	
	public PatientEntryActivity(IShell parent) {
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub
		
	}
	
	public int parsePatientFromModel(DataModelTree tree, TreeReference patRef) {
		Patient newPatient = new Patient();
		
		String id = (String)getValue("/patients/patient/id", patRef, tree);
		String familyName = (String)getValue("/patients/patient/name/family", patRef, tree);
		String givenName = (String)getValue("/patients/patient/name/given", patRef, tree);
		String middleName = (String)getValue("/patients/patient/name/middle", patRef, tree);
		String sexStr = ((Selection)getValue("/patients/patient/sex", patRef, tree)).getValue();
		int sex = ("m".equals(sexStr) ? Patient.SEX_MALE : "f".equals(sexStr) ? Patient.SEX_FEMALE : Patient.SEX_UNKNOWN);
		Date birth = (Date)getValue("/patients/patient/dob", patRef, tree);
		Date treatmentStart = (Date)getValue("/patients/patient/treatment-start", patRef, tree);

		newPatient.setPatientIdentifier(id);
		newPatient.setGivenName(givenName);
		newPatient.setFamilyName(familyName);
		newPatient.setMiddleName(middleName);
		newPatient.setGender(sex);
		newPatient.setBirthDate(birth);
		newPatient.setTreatmentStartDate(treatmentStart);
		
		NumericListData weightRecords = new NumericListData();
		Vector weights = tree.expandReference(newRef("/patients/patient/weight-history/reading").contextualize(patRef));
		for (int i = 0; i < weights.size(); i++) {
			TreeReference readingRef = (TreeReference)weights.elementAt(i);
			DateValueTuple reading = new DateValueTuple();
			reading.value = ((Integer)getValue("/patients/patient/weight-history/reading/weight", readingRef, tree)).intValue();
			reading.date = (Date)getValue("/patients/patient/weight-history/reading/taken-on", readingRef, tree);
			weightRecords.addMeasurement(reading);
		}
		newPatient.setRecord("weight", weightRecords);
		
		if("SINGLE".equals(context.getElement("ENTRY_MODE"))){
			this.context.setElement("PATIENT_ID", new Integer(newPatient.getRecordId()));
		}
		
		return writePatient(newPatient);
	}
	
	private int writePatient(Patient newPatient) {
		PatientRMSUtility utility = (PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName());
		return utility.writeToRMS(newPatient);
	}
	
	private Object getValue (String xpath, TreeReference context, DataModelTree tree) {
		IAnswerData val = tree.resolveReference(newRef(xpath).contextualize(context)).getValue();
		return (val == null ? null : val.getValue());
	}
	
	private TreeReference newRef (String xpath) {
		return DataModelTree.unpackReference(new XPathReference(xpath));
	}
	
	public int parsePatientsFromModel(DataModelTree tree) {
		int patID = -1;
		
		Vector patientRefs = tree.expandReference(newRef("/patients/patient"));
		for (int i = 0; i < patientRefs.size(); i++) {
			int newPatID = parsePatientFromModel(tree, (TreeReference)patientRefs.elementAt(i));
			if (patID == -1)
				patID = newPatID;
		}
		
		return patID;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		int patID = parsePatientsFromModel((DataModelTree)patientEntryForm.getDataModel());
		Hashtable returnVals = new Hashtable();
		returnVals.put(NEW_PATIENT_ID, new Integer(patID));
		parent.returnFromActivity(this,Constants.ACTIVITY_COMPLETE, returnVals);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = context;
		String mode = (String)context.getElement("ENTRY_MODE");
		if(mode.equals("BATCH")) {
			patientEntryForm = XFormUtils.getFormFromResource("/batch-patient-entry.xhtml");
				
		} else {
			patientEntryForm = XFormUtils.getFormFromResource("/patient-entry.xhtml");
		}
		
		Hashtable table = new Hashtable();
		table.put(PATIENT_ENTRY_FORM_KEY, patientEntryForm);
		parent.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION, table);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
