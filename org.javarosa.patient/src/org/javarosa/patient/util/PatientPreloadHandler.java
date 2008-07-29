package org.javarosa.patient.util;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.patient.model.Patient;

public class PatientPreloadHandler implements IPreloadHandler {
	
	/** The patient for this handler */
	private Patient patient;
	
	/**
	 * Creates a preload handler that can pull values from
	 * the patient object provided.
	 * 
	 * @param thePatient the patient whose data is to be 
	 * retrevied from this preload handler
	 */
	public PatientPreloadHandler(Patient thePatient) {
		patient = thePatient;
	}
	
	/**
	 * 
	 */
	public IAnswerData handle(String preloadParams) {
		IAnswerData returnVal = null;
		if(preloadParams == "monthsOnTreatment") {
			//TODO: Get actual data from patient
			returnVal = new IntegerData(12);
		}
		return returnVal;
	}

}
