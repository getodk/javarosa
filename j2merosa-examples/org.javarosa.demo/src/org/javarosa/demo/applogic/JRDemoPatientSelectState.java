package org.javarosa.demo.applogic;

import org.javarosa.entity.api.EntitySelectController;
import org.javarosa.entity.api.EntitySelectState;
import org.javarosa.patient.entry.activity.PatientEntryState;
import org.javarosa.patient.model.Patient;

public class JRDemoPatientSelectState extends EntitySelectState<Patient> {

	protected EntitySelectController<Patient> getController() {
		return null;
	}

	public void cancel() {
		JRDemoContext._().setPatientID(-1);
		new JRDemoSplashScreenState().done(); //login screen may be skipped depending on build flag
	}

	public void entitySelected(int id) {
		JRDemoContext._().setPatientID(id);
		new JRDemoFormListState().start();
	}

	public void newEntity() {
		final JRDemoPatientSelectState patSel = this;
		new PatientEntryState () {
			public void onward(int recID) {
				patSel.newEntityCreated(recID);
			}

			public void abort() {
				patSel.newEntityCreated(-1);				
			}
		}.start();
	}

	public void empty() {
		throw new RuntimeException("transition not applicable");
	}

}
