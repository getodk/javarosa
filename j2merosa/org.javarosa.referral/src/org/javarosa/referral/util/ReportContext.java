package org.javarosa.referral.util;

import org.javarosa.core.Context;

public class ReportContext extends Context {
	private final static String FORM_NAME_KEY = "FORM_NAME";
	private final static String MODEL_ID_KEY = "MODEL_ID";
	
	public void setFormName(String formName) {
		this.contextObject.put(FORM_NAME_KEY, formName);
	}
	
	public String getFormName() {
		return (String)this.contextObject.get(FORM_NAME_KEY);
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
