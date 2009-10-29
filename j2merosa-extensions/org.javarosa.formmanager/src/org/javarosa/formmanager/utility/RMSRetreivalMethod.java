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

import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageManager;

public class RMSRetreivalMethod implements IFormDefRetrievalMethod {
	private FormDef def;

	public RMSRetreivalMethod(int formId) {
		load(formId);
	}
	
	public RMSRetreivalMethod(String formName) {
		IStorageUtilityIndexed forms = (IStorageUtilityIndexed)StorageManager.getStorage(FormDef.STORAGE_KEY);
		int id;
		
        Vector IDs = forms.getIDsForValue("DESCRIPTOR", formName);
        if (IDs.size() == 1) {
        	id = ((Integer)IDs.elementAt(0)).intValue();
        } else {
        	throw new RuntimeException("No form found for descriptor [" + formName + "]");
        }

        load(id);
	}
	
	private void load(int id) {
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		FormDef theForm = (FormDef)forms.read(id);
		
		if (theForm != null) {
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
