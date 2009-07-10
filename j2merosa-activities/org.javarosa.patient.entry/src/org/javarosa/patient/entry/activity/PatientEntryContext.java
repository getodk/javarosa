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

/**
 * 
 */
package org.javarosa.patient.entry.activity;

import org.javarosa.core.Context;
import org.javarosa.core.model.utils.IModelProcessor;

/**
 * @author Clayton Sims
 * @date Jan 27, 2009 
 *
 */
public class PatientEntryContext extends Context {
	public static final String TITLE_KEY = "pec_tk";
	public static final String PROCESSOR_KEY = "pec_pk";

	public PatientEntryContext(Context context) {
		super(context);
	}
	
	public void setEntryFormTitle(String title) {
		this.setElement(TITLE_KEY, title);
	}
	
	public String getEntryFormTitle() {
		return (String)this.getElement(TITLE_KEY);
	}
	
	public void setProcessor(IModelProcessor processor) {
		this.setElement(PROCESSOR_KEY, processor);
	}
	
	public IModelProcessor getProcessor() {
		IModelProcessor p = (IModelProcessor)getElement(PROCESSOR_KEY);
		if(p != null) {
			return p;
		} else {
			return new PatientEntryModelProcessor();
		}
	}
}
