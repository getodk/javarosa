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

import javax.microedition.lcdui.Command;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.FormElementBinding;

public class FormEntryController {
	public static final int QUESTION_OK = 0;
	public static final int QUESTION_REQUIRED_BUT_EMPTY = 1;
	public static final int QUESTION_CONSTRAINT_VIOLATED = 2;
	
	FormEntryModel model;
	IFormEntryView view;
	IControllerHost parent;

	public FormEntryController (FormEntryModel model, IControllerHost parent) {
		this.model = model;
		this.parent = parent;
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
			DataModelTreeRMSUtility utility = (DataModelTreeRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(DataModelTreeRMSUtility.getUtilityName());
			DataModelTree instance = (DataModelTree)form.getDataModel();
			int instanceID = model.getInstanceID();

			instance.setName(form.getTitle());
	        instance.setFormId(form.getRecordId());
	        instance.setDateSaved(new Date());

			if(instanceID == -1) {
				instanceID = utility.writeToRMS(instance);
			} else {
				utility.updateToRMS(instanceID, instance);
			}

			model.modelSaved(instanceID);
		}
	}

	public void exit () {
		this.exit("exit");
	}
	public void exit (String code) {
		view.destroy();
		parent.controllerReturn(code);
	}

	public void startOver () {

	}

	public void setLanguage (String language) {
		model.getForm().getLocalizer().setLocale(language);
	}

	public void cycleLanguage () {
		setLanguage(model.getForm().getLocalizer().getNextLocale());
	}

	public void setView (IView d) {
		parent.setView(d);
	}
	public void suspendActivity(Command command) {
		// added for image choosing
		parent.controllerReturn(command.getLabel());
	}
}