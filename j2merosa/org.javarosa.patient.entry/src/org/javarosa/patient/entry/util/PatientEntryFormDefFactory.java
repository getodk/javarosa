package org.javarosa.patient.entry.util;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.patient.entry.model.PatientReplicatorTrigger;

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
