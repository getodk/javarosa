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

package org.javarosa.workflow;

import org.javarosa.core.Context;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.workflow.model.IActionProcessor;

public class PostActionProcessContext extends Context {
	private static String ACTION_PROCESS_KEY = "ap";
	private static String DATA_MODEL_KEY = "dm";
	
	public void setActionProcessor(IActionProcessor ap) {
		this.setElement(ACTION_PROCESS_KEY, ap);
	}
	
	public IActionProcessor getActionProcessor() {
		return (IActionProcessor)this.getElement(ACTION_PROCESS_KEY);
	}
	
	public void setDataModel(IFormDataModel dm) {
		this.setElement(DATA_MODEL_KEY, dm);
	}
	
	public IFormDataModel getDataModel() {
		return (IFormDataModel)this.getElement(DATA_MODEL_KEY);
	}
}
