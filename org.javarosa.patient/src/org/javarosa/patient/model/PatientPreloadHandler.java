package org.javarosa.patient.model;

import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.patient.model.data.NumericListData;

public class PatientPreloadHandler implements IPreloadHandler {
	private Patient patient;

	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handle(java.lang.String)
	 */
	public IAnswerData handle(String preloadParams) {
		IAnswerData result;
		int selectorIndex = preloadParams.indexOf('[');
		if(selectorIndex == -1) { 
			Object record = patient.getRecord(preloadParams);
			result = new StringData((String)record);
		}
		else {
			Vector results = patient.getRecordSet(preloadParams.substring(0, selectorIndex),
					preloadParams.substring(selectorIndex, preloadParams.length()));
			result = new NumericListData();
			result.setValue(results);
		}
		return result;
	}
}
