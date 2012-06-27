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

import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;

public class FormDefFetcher {
	IFormDefRetrievalMethod fetcher;
	Vector<IPreloadHandler> preloadHandlers;
	Vector<IFunctionHandler> funcHandlers;
	
	FormInstance instance;
	
	public FormDefFetcher(IFormDefRetrievalMethod retriever,
			Vector<IPreloadHandler> preloadHandlers, Vector <IFunctionHandler> funcHandlers) {
		this.fetcher = retriever;
		this.preloadHandlers = preloadHandlers;
		this.funcHandlers = funcHandlers;
	}
	
	public FormDefFetcher(IFormDefRetrievalMethod retriever, int instanceId,
			Vector<IPreloadHandler> preloadHandlers, Vector <IFunctionHandler> funcHandlers) {
		this(retriever, preloadHandlers, funcHandlers);
		loadModel(instanceId);
	}
	
	private void loadModel(int instanceId) {
		IStorageUtility instances = StorageManager.getStorage(FormInstance.STORAGE_KEY);
		instance = (FormInstance)instances.read(instanceId);
	}

	public FormDef getFormDef() {
		FormDef form = fetcher.retreiveFormDef(); 
		if(instance != null) {
			form.setInstance(instance);
		}
		
		//A lot of this should probably not be with the form.
		initPreloadHandlers(form);
		form.setEvaluationContext(initEvaluationContext(new EvaluationContext(null)));
		form.initialize(instance == null);
		
		return form;
	}
	
	private void initPreloadHandlers (FormDef f) {
		if(preloadHandlers != null) {
			for (int i = 0; i < preloadHandlers.size(); i++) {
				f.getPreloader().addPreloadHandler(preloadHandlers.elementAt(i));
			}
		}
	}
	
	private EvaluationContext initEvaluationContext (EvaluationContext ec) {
		if(funcHandlers != null) {
			for (int i = 0; i < funcHandlers.size(); i++) {
				ec.addFunctionHandler(funcHandlers.elementAt(i));
			}
		}
		
		return ec;
	}


}
