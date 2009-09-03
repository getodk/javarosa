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

package org.javarosa.formmanager.utility;

import java.io.IOException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;

public class RMSRetreivalMethod implements IFormDefRetrievalMethod {
	private FormDef def;

	public RMSRetreivalMethod(int formId) throws IOException, DeserializationException {
		load(formId);
	}
	
	public RMSRetreivalMethod(String formName) throws IOException, DeserializationException {
		FormDefRMSUtility formUtil = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());  //whoa!
		load(formUtil.getIDfromName(formName));
	}
	
	private void load(int id) throws IOException, DeserializationException  {
		FormDefRMSUtility formUtil = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());  //whoa!
		FormDef theForm = new FormDef();
		formUtil.retrieveFromRMS(id, theForm);
		// TODO: Better heuristic for whether retrieval worked!
		if (theForm.getID() != -1) {
			this.def = theForm;
		} else {
			String error = "Form loader couldn't retrieve form for ";
			error += " ID = " + id;
			throw new RuntimeException(error);
		}
	}
	
	public FormDef retreiveFormDef() {
		return def;
	}
}
