package org.javarosa.patient.entry.activity;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.patient.entry.util.PatientEntryFormDefFactory;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.model.data.NumericListData;
import org.javarosa.patient.storage.PatientRMSUtility;
import org.javarosa.patient.util.DateValueTuple;

/**
 * NOTICE:
 * 
 * 
 * This class, and the entire org.javarosa.patient.entry Module all only
 * exist because <group> and <repeat> are not yet available in our XForms 
 * subset. 
 * 
 * THIS MODULE IS FULL OF CODE THAT SHOULD NOT BE EMULATED.
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

	public static final String PATIENT_ENTRY_FORM_KEY = "Patient Entry Form";
	
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
	
	public void parsePatientFromModel(QuestionDataGroup patient) {
		Patient newPatient = new Patient();
		
		NumericListData weightRecords = new NumericListData();
		
		Vector children = patient.getChildren();
		Enumeration en = children.elements();
		
		boolean formComplete = false;
		while(en.hasMoreElements()) {
			TreeElement element = (TreeElement)en.nextElement();
			if(element instanceof QuestionDataElement) {
				QuestionDataElement data  = (QuestionDataElement)element;
				if("GivenName".equals(data.getName())) {
					StringData name = (StringData)data.getValue();
					if(name != null) {
						newPatient.setGivenName((String)name.getValue());
					}
				} else if("FamilyName".equals(data.getName())) {
					StringData name = (StringData)data.getValue();
					if(name != null) {
						newPatient.setFamilyName((String)name.getValue());
					}
				} else if("MiddleName".equals(data.getName())) {
					StringData name = (StringData)data.getValue();
					if(name != null) {
						newPatient.setMiddleName((String)name.getValue());
					}
				} else if("DOB".equals(data.getName())) {
					DateData date = (DateData)data.getValue();
					if(date != null) {
						newPatient.setBirthDate((Date)date.getValue());
					}
				} else if("TreatmentStartDate".equals(data.getName())) {
					DateData date = (DateData)data.getValue();
					if(date != null) {
						newPatient.setTreatmentStartDate((Date)date.getValue());
					}
				} else if("MorePatients".equals(data.getName())) {
					if(data.getValue() != null) {
						formComplete = true;
					}
				}
			} else if(element instanceof QuestionDataGroup) {
				QuestionDataGroup group = (QuestionDataGroup)element;
				if(group.getName().equals("Weight")) {
					Enumeration weights = group.getChildren().elements();
					DateValueTuple tuple = null;
					while(weights.hasMoreElements()) {
						QuestionDataElement weightData = (QuestionDataElement)weights.nextElement();
						if(weightData.getName().indexOf("Value") != -1 ) {
							tuple = new DateValueTuple();
							Integer answer = (Integer)weightData.getValue().getValue();
							if(answer != null) {
								tuple.value = answer.intValue();
							}
						}
						if(weightData.getName().indexOf("Date") != -1 ) {
							Date answer = (Date)weightData.getValue().getValue();
							if(answer != null && tuple != null) {
								tuple.date = answer;
								weightRecords.addMeasurement(tuple);
							}
						}
					}
				}
			}
		}
		newPatient.setRecord("weight", weightRecords);
		if(formComplete) {
			writePatient(newPatient);
		}
	}
	
	private void writePatient(Patient newPatient) {
		PatientRMSUtility utility = (PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName());
		utility.writeToRMS(newPatient);
	}
	
	public void parsePatientsFromModel(DataModelTree tree) {
		QuestionDataGroup group = (QuestionDataGroup)tree.getRootElement();
		Enumeration en = group.getChildren().elements();
		while(en.hasMoreElements()) {
			parsePatientFromModel((QuestionDataGroup)en.nextElement());
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		parsePatientsFromModel((DataModelTree)patientEntryForm.getDataModel());
		parent.returnFromActivity(this,Constants.ACTIVITY_COMPLETE, null);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = context;
		patientEntryForm = PatientEntryFormDefFactory.createPatientEntryFormDef();
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
	
}
