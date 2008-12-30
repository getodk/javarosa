package org.javarosa.patient;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.patient.storage.PatientRMSUtility;

public class PatientModule implements IModule {

	public void registerModule(Context context) {
		PatientRMSUtility patientRms = new PatientRMSUtility(PatientRMSUtility.getUtilityName());
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(patientRms);
	}
   
}
