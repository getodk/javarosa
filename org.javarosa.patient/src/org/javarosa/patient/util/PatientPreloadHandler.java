package org.javarosa.patient.util;

import java.util.Date;
import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.model.data.ImmunizationAnswerData;
import org.javarosa.patient.model.data.ImmunizationData;
import org.javarosa.patient.model.data.NumericListData;

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
	public IAnswerData handlePreload(String preloadParams) {
		IAnswerData returnVal = null;
		if("vaccination_table".equals(preloadParams)) {
			ImmunizationData table = patient.getVaccinations();
			returnVal = new ImmunizationAnswerData(table);
		} else if(preloadParams.equals("monthsOnTreatment")) {
			DateData dateData = (DateData)patient.getRecord("treatmentStart");
			if(dateData != null) {
				int months = DateUtils.getMonthsDifference((Date)dateData.getValue(), new Date()); 
				returnVal = new IntegerData(months);
			}
		}
		else {
			int selectorStart = preloadParams.indexOf("[");
			if(selectorStart == -1) {
				returnVal = (IAnswerData)patient.getRecord(preloadParams);
			} else {
				String type = preloadParams.substring(0, selectorStart);
				String selector = preloadParams.substring(selectorStart, preloadParams.length());
				Vector data = patient.getRecordSet(type, selector);
				returnVal = new NumericListData();
				returnVal.setValue(data);
			}
		}
		return returnVal;
	}

	public boolean handlePostProcess(IFormDataModel model, IDataReference ref, String params) {
		IAnswerData data = model.getDataValue(ref);

		if ("vaccination_table".equals(params)) {
			patient.setVaccinations((ImmunizationData)((ImmunizationAnswerData)data).getValue());
			return true;
		} else {
			int selectorStart = params.indexOf("[");
			if(selectorStart == -1) {
				//modifying such values not supported right now
			} else {
				String type = params.substring(0, selectorStart);
				NumericListData combinedList = new NumericListData();
				combinedList.setValue(patient.getRecordSet(type, "[0:N]"));
				combinedList.mergeList((NumericListData)data);
				patient.setRecord(type, combinedList);
			}
		}
		
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "patient";
	}

}
