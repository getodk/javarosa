package org.javarosa.patient.entry.activity;

import java.util.Date;
import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.IModelProcessor;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.model.data.NumericListData;
import org.javarosa.patient.storage.PatientRMSUtility;
import org.javarosa.patient.util.DateValueTuple;

public class PatientEntryModelProcessor implements IModelProcessor {
	
	int patId;

	public void processModel(DataModelTree tree) {
		patId = -1;
		
		Vector patientRefs = tree.expandReference(newRef("/patients/patient"));
		for (int i = 0; i < patientRefs.size(); i++) {
			int newPatID = parsePatientFromModel(tree, (TreeReference)patientRefs.elementAt(i));
			if (patId == -1)
				patId = newPatID;
		} 
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
	
	public int getPatId() {
		return patId;
	}

	public void initializeContext(Context context) {
		// TODO Auto-generated method stub
		
	}

	public void loadProcessedContext(Context context) {
		context.setElement("PATIENT_ID", new Integer(patId));
	}
}
