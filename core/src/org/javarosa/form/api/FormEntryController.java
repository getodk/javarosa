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

package org.javarosa.form.api;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;

public class FormEntryController {
	public static final int QUESTION_OK = 0;
	public static final int QUESTION_REQUIRED_BUT_EMPTY = 1;
	public static final int QUESTION_CONSTRAINT_VIOLATED = 2;
	
	public static final int BEGINNING_OF_FORM_EVENT = 0;
	public static final int END_OF_FORM_EVENT = 1;
	public static final int PROMPT_NEW_REPEAT_EVENT = 2;
	public static final int QUESTION_EVENT = 4;
	public static final int GROUP_EVENT = 8;
	public static final int REPEAT_EVENT = 16;
	
	FormEntryModel model;
	
	public FormEntryController (FormEntryModel model, boolean readOnly) {
		this.model = model;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.State#start()
	 */
	public void start() {
		
	}
	
	public FormEntryModel getModel() {
		return model;
	}

	public int questionAnswered (FormIndex index, IAnswerData data) {
		TreeElement element = model.getTreeElement(index);
		if (element.required && data == null) {
			return QUESTION_REQUIRED_BUT_EMPTY;
		} else if (!model.getForm().evaluateConstraint(index.getReference(), data)) {
			return QUESTION_CONSTRAINT_VIOLATED;
		} else {
			commitAnswer(element, index, data);
			return QUESTION_OK;
		}
	}

	//TODO: constraint isn't checked here, meaning if you 'save' on a question with invalid data entered in, that data will save
	//without complaint... seems wrong (but oh-so right?)
	private boolean commitAnswer (TreeElement element, FormIndex index,IAnswerData data) {
		if (data != null || element.getValue() != null) {
			//we should check if the data to be saved is already the same as the data in the model, but we can't (no IAnswerData.equals())
			model.getForm().setValue(data, index.getReference(), element);
			return true;
		} else {
			return false;
		}
	}

	public int stepToNextEvent () {
		return stepEvent(true);
	}
	
	public int stepPreviousEvent () {
		return stepEvent(false);
	}
	
	private int stepEvent(boolean forward) {
		FormIndex index = model.getCurrentFormIndex();

		do {
			if (forward) {
				index = model.getForm().incrementIndex(index);
			} else {
				index = model.getForm().decrementIndex(index);
			}
			//System.out.println("Question Index: " + index.toString() + " relevancy is: " + model.isRelevant(index));
		} while (index.isInForm() && !model.isRelevant(index));
	
		return jumpToIndex(index);
	}
	
	public int jumpToIndex(FormIndex index) {
		model.setQuestionIndex(index);
		
		return model.getEvent(index);
	}

	public void newRepeat (FormIndex questionIndex) {
		model.getForm().createNewRepeat(questionIndex);
	}
	
	public FormIndex deleteRepeat (FormIndex questionIndex) {
		return model.getForm().deleteRepeat(questionIndex);
	}
	
	public void setLanguage(String language) {
		model.setCurrentLanguage(language);
	}
}