package org.javarosa.referral.util;

import org.javarosa.core.Context;

public class ReportContext extends Context {
	private final static String FORM_ID_KEY = "FORM_ID";
	private final static String MODEL_ID_KEY = "MODEL_ID";
	
	public void setFormId(int formId) {
		this.contextObject.put(FORM_ID_KEY, new Integer(formId));
	}
	
	/**
	 * @return the patient Id for the current patient. -1 if no
	 * patient is currently selected
	 */
	public int getFormId() {
		Integer id = (Integer)this.contextObject.get(FORM_ID_KEY);
		if(id == null ) {
			return -1;
		}
		else {
			return id.intValue();
		}
	}
	
	public void setModelId(int modelId) {
		this.contextObject.put(MODEL_ID_KEY, new Integer(modelId));
	}
	
	/**
	 * @return the model Id for the current model. -1 if no
	 * model is currently selected
	 */
	public int getModelId() {
		Integer id = (Integer)this.contextObject.get(MODEL_ID_KEY);
		if(id == null ) {
			return -1;
		}
		else {
			return id.intValue();
		}
	}
}
