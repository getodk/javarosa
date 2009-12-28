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

package org.javarosa.formmanager.controller;

import java.util.Date;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.IFormEntryViewFactory;
import org.javarosa.j2me.services.DataCaptureService;
import org.javarosa.j2me.services.DataCaptureServiceRegistry;
import org.javarosa.j2me.view.J2MEDisplay;

public class FormEntryController {
	public static final int QUESTION_OK = 0;
	public static final int QUESTION_REQUIRED_BUT_EMPTY = 1;
	public static final int QUESTION_CONSTRAINT_VIOLATED = 2;
	
	FormEntryModel model;
	IFormEntryView view;
	DataCaptureServiceRegistry dataCapture;
	
	FormEntryTransitions transitions;
	
	public FormEntryController (IFormEntryViewFactory viewFactory, FormDefFetcher fetcher, boolean readOnly) {
		this(-1, viewFactory, fetcher, readOnly, null, null);
	}

	public FormEntryController (int savedInstanceID, IFormEntryViewFactory viewFactory, FormDefFetcher fetcher, boolean readOnly) {
		this(savedInstanceID, viewFactory, fetcher, readOnly, null, null);
	}	
	
	public FormEntryController (int savedInstanceID, IFormEntryViewFactory viewFactory, FormDefFetcher fetcher, boolean readOnly,
			FormIndex firstQuestion, DataCaptureServiceRegistry dataCapture) {
		FormDef theForm = fetcher.getFormDef();
		this.dataCapture = dataCapture;
		
		//droos 10/29: what about loading in the old saved instance?
		
		model = new FormEntryModel(theForm, savedInstanceID, firstQuestion, readOnly);
		
		//the view constructor MUST call setFormEntryView before it makes any calls to the controller
		//pretty confusing, but that's how it works right now
		viewFactory.getFormEntryView(model, this);
	}
	
	public void setTransitions (FormEntryTransitions transitions) {
		this.transitions = transitions;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.State#start()
	 */
	public void start() {
		view.show();
	}
	
	public void setFormEntryView (IFormEntryView view) {
		this.view = view;
	}

	public int questionAnswered (FormElementBinding binding, IAnswerData data) {
		if (binding.instanceNode.required && data == null) {
			return QUESTION_REQUIRED_BUT_EMPTY;
		} else if (!model.getForm().evaluateConstraint(binding.instanceRef, data)) {
			return QUESTION_CONSTRAINT_VIOLATED;
		} else {
			commitAnswer(binding, data);
			stepQuestion(true);
			return QUESTION_OK;
		}
	}

	//TODO: constraint isn't checked here, meaning if you 'save' on a question with invalid data entered in, that data will save
	//without complaint... seems wrong (but oh-so right?)
	public boolean commitAnswer (FormElementBinding binding, IAnswerData data) {
		if (data != null || binding.getValue() != null) {
			//we should check if the data to be saved is already the same as the data in the model, but we can't (no IAnswerData.equals())
			model.getForm().setValue(data, binding.instanceRef, binding.instanceNode);
			model.modelChanged();
			return true;
		} else {
			return false;
		}
	}

	public void stepQuestion (boolean forward) {
		FormIndex index = model.getQuestionIndex();

		do {
			if (forward) {
				index = model.getForm().incrementIndex(index);
			} else {
				index = model.getForm().decrementIndex(index);
			}
			//System.out.println("Question Index: " + index.toString() + " relevancy is: " + model.isRelevant(index));
		} while (index.isInForm() && !model.isRelevant(index));

		if (index.isBeginningOfFormIndex()) {
			//already at the earliest relevant question
			model.notifyStartOfForm();
			return;
		} else if (index.isEndOfFormIndex()) {
			model.setFormComplete();
			return;
		}

		selectQuestion(index);
	}

	public void selectQuestion (FormIndex questionIndex) {
		model.setQuestionIndex(questionIndex);
	}

	public void newRepeat (FormIndex questionIndex) {
		model.getForm().createNewRepeat(questionIndex);
	}
	
	//saves model as is; view is responsible for committing any pending data in the current question
	public void save () {
		boolean postProcessModified = model.getForm().postProcessModel();

		if (!model.isSaved() || postProcessModified) {
			FormDef form = model.getForm();
			IStorageUtility instances = StorageManager.getStorage(DataModelTree.STORAGE_KEY);
			DataModelTree instance = (DataModelTree)form.getDataModel();

			instance.setName(form.getTitle());
	        instance.setFormId(form.getID());
	        instance.setDateSaved(new Date());

	        try {
	        	instances.write(instance);
    		} catch (StorageFullException e) {
    			throw new RuntimeException("uh-oh, storage full [datamodeltrees]"); //TODO: handle this
    		}

			model.modelSaved(instance.getID());
		}
	}

	public void exit () {
		view.destroy();

		if(!model.isSaved()) {
			transitions.abort();
		} else {
			transitions.formEntrySaved(model.getForm(), model.getForm().getDataModel(), model.isFormComplete());
		}
	}
	
	public void setLanguage (String language) {
		model.getForm().getLocalizer().setLocale(language);
	}

	public void cycleLanguage () {
		setLanguage(model.getForm().getLocalizer().getNextLocale());
	}

	public void setView (Displayable view) {
		J2MEDisplay.setView(view);
	}
	
	public DataCaptureService getDataCaptureService (String type) throws UnavailableServiceException {
		if (dataCapture == null) {
			throw new UnavailableServiceException("Data capture services have not been provided");
		} else {
			return dataCapture.getService(type);
		}
	}
	
	// added for image choosing
	public void suspendActivity(int mediaType) {
		transitions.suspendForMediaCapture(mediaType);
	}
	
}