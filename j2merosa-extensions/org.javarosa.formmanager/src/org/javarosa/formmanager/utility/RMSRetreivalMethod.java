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

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.formmanager.activity.FormEntryContext;

public class RMSRetreivalMethod implements IFormDefRetrievalMethod {

	public FormDef retreiveFormDef(Context context) {
		if(context instanceof FormEntryContext) {
			FormEntryContext formContext = (FormEntryContext)context;

			//TODO: Are we going to make this non-RMS dependant any any point?
			FormDefRMSUtility formUtil = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());  //whoa!
			FormDef theForm = new FormDef();
			try {
				if(formContext.getFormID() != -1) {
					formUtil.retrieveFromRMS(formContext.getFormID(), theForm);
				} else if(formContext.getFormName() != null) {
					formUtil.retrieveFromRMS(formUtil.getIDfromName(formContext.getFormName()), theForm);
				}
				//TODO: Better heuristic for whether retrieval worked!
				if(theForm.getID() != -1) {
					return theForm;
				} else {
					String error = "Form loader couldn't retrieve form for ";
					if(formContext.getFormID() != -1) {
						error += " ID = " + formContext.getFormID();
					} else if(formContext.getFormName() != null) {
						error += " Name = " + formContext.getFormName();
					}
					throw new RuntimeException(error);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DeserializationException uee) {
				uee.printStackTrace();
			} 
		}
		return null;
	}

}
