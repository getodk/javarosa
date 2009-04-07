/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.formmanager.activity;

import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.utils.IModelProcessor;
import org.javarosa.core.model.utils.IPreloadHandler;

public class FormEntryContext extends Context {
	public static final String FORM_ID = "FORM_ID";
	public static final String FORM_NAME = "FORM_NAME";
	public static final String INSTANCE_ID = "INSTANCE_ID";
	public static final String PRELOAD_HANDLERS = "PRELOAD_HANDLERS";
	public static final String FUNCTION_HANDLERS = "FUNCTION_HANDLERS";
	public static final String READ_ONLY = "fec_ro";
	public static final String FIRST_QUESTION_INDEX = "fec_fqi";
	public static final String MODEL_PROCESSOR_KEY = "fec_modelproc";
	
	public FormEntryContext(Context context) { 
		super(context);
	}
	
	public int getFormID () {
		return ((Integer)getElement(FORM_ID)).intValue();
	}
	
	public void setFormID (int formID) {
		setElement(FORM_ID, new Integer(formID));
	}
	
	public String getFormName () {
		return (String)getElement(FORM_NAME);
	}
	
	public void setFormName (String formName) {
		setElement(FORM_NAME, formName);
	}

	public int getInstanceID () {
		Integer i = (Integer)getElement(INSTANCE_ID);
		return (i == null ? -1 : i.intValue());
	}
	
	public void setInstanceID (int instanceID) {
		setElement(INSTANCE_ID, new Integer(instanceID));
	}
	
	public void addHandler (Object handler, String key) {
		Vector handlers = (Vector) getElement(key);
		if(handlers == null) {
			handlers = new Vector();
		}
		handlers.addElement(handler);
		setElement(key, handlers);	
	}
	
	public void addPreloadHandler(IPreloadHandler handler) { 
		addHandler(handler, PRELOAD_HANDLERS);
	}
	
	public Vector getPreloadHandlers() {
		return (Vector)getElement(PRELOAD_HANDLERS);
	}
	
	public void addFunctionHandler(IFunctionHandler handler) {
		addHandler(handler, FUNCTION_HANDLERS);
	}
	
	public Vector getFunctionHandlers() {
		return (Vector)getElement(FUNCTION_HANDLERS);
	}
	
	public void setReadOnly(boolean readonly) {
		setElement(READ_ONLY, new Boolean(readonly));
	}
	public boolean getReadOnly() {
		Boolean readOnly = (Boolean) getElement(READ_ONLY);
		if(readOnly != null) {
			return readOnly.booleanValue();
		} else {
			return false;
		}
	}
	
	public void setFirstQuestionIndex(FormIndex index) {
		setElement(FIRST_QUESTION_INDEX, index);
	}
	
	public FormIndex getFirstQuestionIndex() {
		return (FormIndex)getElement(FIRST_QUESTION_INDEX);
	}
	
	public void setModelProcessor(IModelProcessor proc) {
		this.setElement(MODEL_PROCESSOR_KEY, proc);
	}
	
	public IModelProcessor getModelProcessor() {
		return (IModelProcessor)this.getElement(MODEL_PROCESSOR_KEY);
	}
}