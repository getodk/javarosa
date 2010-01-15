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
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.externalizable.DeserializationException;

public class FormDefFetcher {
	IFormDefRetrievalMethod fetcher;
	Vector preloadHandlers;
	
	FormInstance instance;
	
	public FormDefFetcher(IFormDefRetrievalMethod retriever, Vector preloadHandlers) {
		this.fetcher = retriever;
		this.preloadHandlers = preloadHandlers; 	
	}
	
	public FormDefFetcher(IFormDefRetrievalMethod retriever, Vector preloadHandlers, int instanceId) {
		this(retriever, preloadHandlers);
		loadModel(instanceId);
	}
	
	private void loadModel(int instanceId) {
		IStorageUtility instances = StorageManager.getStorage(FormInstance.STORAGE_KEY);
		instance = (FormInstance)instances.read(instanceId);
	}

	public FormDef getFormDef() {
		FormDef form = fetcher.retreiveFormDef(); 
		if(instance != null) {
			form.setDataModel(instance);
		}
		
		//A lot of this should probably not be with the form.
		initPreloadHandlers(form);
		form.initialize(instance == null);
		form.setEvaluationContext(initEvaluationContext());
		
		return form;
	}
	
	private void initPreloadHandlers (FormDef f) {
		if(preloadHandlers != null) {
			Enumeration en = preloadHandlers.elements();
			while(en.hasMoreElements()) {
				f.getPreloader().addPreloadHandler((IPreloadHandler)en.nextElement());
			}
		}
	}
	
	private EvaluationContext initEvaluationContext () {
		EvaluationContext ec = new EvaluationContext();
		
		Vector functionHandlers = new Vector(); //get this vector
		if(functionHandlers != null) {
			Enumeration en = functionHandlers.elements();
			while(en.hasMoreElements()) {
				ec.addFunctionHandler((IFunctionHandler)en.nextElement());
			}
		}
		
		return ec;
	}


}
