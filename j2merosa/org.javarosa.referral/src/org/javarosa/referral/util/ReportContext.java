/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.referral.util;

import org.javarosa.core.Context;

public class ReportContext extends Context {
	private final static String FORM_NAME_KEY = "FORM_NAME";
	private final static String MODEL_ID_KEY = "MODEL_ID";
	private final static String FORM_ID_KEY = "FORM_ID";
	
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
	
	public void setFormId(int formId) {
		this.contextObject.put(FORM_ID_KEY, new Integer(formId));
	}
	
	/**
	 * @return the model Id for the current model. -1 if no
	 * model is currently selected
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
}
