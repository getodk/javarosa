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

package org.javarosa.patient.entry.activity;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.IModelProcessor;
import org.javarosa.formmanager.api.FormEntryState;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;
import org.javarosa.formmanager.view.chatterbox.util.ChatterboxFactory;

public abstract class PatientEntryState extends FormEntryState {
	protected String singleRegForm = "jr-patient-single-reg";
	protected String batchRegForm = "jr-patient-batch-reg";

	protected String formName;
	protected IModelProcessor processor;
	
	public PatientEntryState () {
		this(new PatientEntryModelProcessor());
	}
	
	public PatientEntryState (IModelProcessor processor) {
		this(processor, false);
	}
	
	public PatientEntryState (IModelProcessor processor, boolean batchMode) {
		this.formName = (batchMode ? batchRegForm : singleRegForm);
		this.processor = processor;
	}
	
	protected FormEntryController getController() {
		FormDefFetcher fetcher = new FormDefFetcher(new RMSRetreivalMethod(formName), null);
		return new FormEntryController(new ChatterboxFactory(), fetcher, false);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.api.transitions.FormEntryTransitions#formEntrySaved(org.javarosa.core.model.FormDef, org.javarosa.core.model.instance.DataModelTree, boolean)
	 */
	public void formEntrySaved(FormDef form, DataModelTree instanceData, boolean formWasCompleted) {
		if (formWasCompleted) {
			processor.processModel(instanceData);
			onward(instanceData.getID());
		} else {
			abort();
		}
	}

	public void suspendForMediaCapture(int captureType) {
		throw new RuntimeException("transition not applicable");
	}

	public abstract void onward (int recID);

}
