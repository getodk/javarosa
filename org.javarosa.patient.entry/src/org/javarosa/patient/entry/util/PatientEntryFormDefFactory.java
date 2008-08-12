package org.javarosa.patient.entry.util;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.patient.entry.model.PatientReplicatorTrigger;

public class PatientEntryFormDefFactory {
	public static FormDef createPatientEntryFormDef() {
		FormDef patientEntryForm = new FormDef();
		DataModelTree tree = new DataModelTree();
		tree.setName("Patients");
		tree.setRootElement(new QuestionDataGroup("Patient"));
		patientEntryForm.setDataModel(tree);
		PatientReplicatorTrigger trigger = new PatientReplicatorTrigger();
		trigger.setTargetForm(patientEntryForm);
		trigger.createPatientQuestionsOnForm();
		return patientEntryForm;
	}
}
