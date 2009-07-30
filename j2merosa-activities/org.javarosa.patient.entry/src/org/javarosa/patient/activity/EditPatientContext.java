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
package org.javarosa.patient.activity;

import org.javarosa.core.Context;

/**
 * @author Clayton Sims
 * @date Mar 6, 2009 
 *
 */
public class EditPatientContext extends Context {
	
	private static final String PATIENT_ID = "pat_id";
	
	public EditPatientContext(Context c) {
		super(c);
	}
	
	public void setPatientId(int id) {
		this.setElement(PATIENT_ID, new Integer(id));
	}
	
	public int getPatientId() {
		Integer id = (Integer)this.getElement(PATIENT_ID);
		if(id == null) {
			return -1;
		} else {
			return id.intValue();
		}
	}

}
