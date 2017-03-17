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
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;

/**
 * This class is used to navigate through an xform and appropriately manipulate
 * the FormEntryModel's state.
 */
public class FormEntryController {
    public static final int ANSWER_OK = 0;
    public static final int ANSWER_REQUIRED_BUT_EMPTY = 1;
    public static final int ANSWER_CONSTRAINT_VIOLATED = 2;

    public static final int EVENT_BEGINNING_OF_FORM = 0;
    public static final int EVENT_END_OF_FORM = 1;
    public static final int EVENT_PROMPT_NEW_REPEAT = 2;
    public static final int EVENT_QUESTION = 4;
    public static final int EVENT_GROUP = 8;
    public static final int EVENT_REPEAT = 16;
    public static final int EVENT_REPEAT_JUNCTURE = 32;

    FormEntryModel model;

    /**
     * Creates a new form entry controller for the model provided
     *
     * @param model
     */
    public FormEntryController(FormEntryModel model) {
        this.model = model;
    }


    public FormEntryModel getModel() {
        return model;
    }

    
    /**
     * Attempts to save answer at the current FormIndex into the datamodel.
     *
     * @param data
     * @return
     */
    public int answerQuestion(IAnswerData data, boolean midSurvey) {
        return answerQuestion(model.getFormIndex(), data, midSurvey);
    }


    /**
     * Attempts to save the answer at the specified FormIndex into the
     * datamodel.
     *
     * @param index
     * @param data
     * @return OK if save was successful, error if a constraint was violated.
     */
    public int answerQuestion(FormIndex index, IAnswerData data, boolean midSurvey) {
    	QuestionDef q = model.getQuestionPrompt(index).getQuestion();
        if (model.getEvent(index) != FormEntryController.EVENT_QUESTION) {
            throw new RuntimeException("Non-Question object at the form index.");
        }
        TreeElement element = model.getTreeElement(index);
        boolean complexQuestion = q.isComplex();

        boolean hasConstraints = false;
        if (element.isRequired() && data == null) {
            return ANSWER_REQUIRED_BUT_EMPTY;
        } else if (!complexQuestion && !model.getForm().evaluateConstraint(index.getReference(), data)) {
            return ANSWER_CONSTRAINT_VIOLATED;
        } else if (!complexQuestion) {
            commitAnswer(element, index, data, midSurvey);
            return ANSWER_OK;
        } else if (complexQuestion && hasConstraints) {
            //TODO: itemsets: don't currently evaluate constraints for itemset/copy -- haven't figured out how handle it yet
            throw new RuntimeException("Itemsets do not currently evaluate constraints. Your constraint will not work, please remove it before proceeding.");
        } else {
        	try {
				model.getForm().copyItemsetAnswer(q, element, data, midSurvey);
			} catch (InvalidReferenceException ire) {
				ire.printStackTrace();
				throw new RuntimeException("Invalid reference while copying itemset answer: " + ire.getMessage());
			}
        	return ANSWER_OK;
        }
    }


    /**
     * saveAnswer attempts to save the current answer into the data model
     * without doing any constraint checking. Only use this if you know what
     * you're doing. For normal form filling you should always use
     * answerQuestion or answerCurrentQuestion.
     *
     * @param index
     * @param data
     * @return true if saved successfully, false otherwise.
     */
    public boolean saveAnswer(FormIndex index, IAnswerData data, boolean midSurvey) {
        if (model.getEvent(index) != FormEntryController.EVENT_QUESTION) {
            throw new RuntimeException("Non-Question object at the form index.");
        }
        TreeElement element = model.getTreeElement(index);
        return commitAnswer(element, index, data, midSurvey);
    }


    /**
     * saveAnswer attempts to save the current answer into the data model
     * without doing any constraint checking. Only use this if you know what
     * you're doing. For normal form filling you should always use
     * answerQuestion().
     *
     * @param data
     * @return true if saved successfully, false otherwise.
     */
    public boolean saveAnswer(IAnswerData data, boolean midSurvey) {
        return saveAnswer(model.getFormIndex(), data, midSurvey);
    }


    /**
     * commitAnswer actually saves the data into the datamodel.
     *
     * @param element
     * @param index
     * @param data
     * @return true if saved successfully, false otherwise
     */
    private boolean commitAnswer(TreeElement element, FormIndex index, IAnswerData data, boolean midSurvey) {
        if (data != null || element.getValue() != null) {
            // we should check if the data to be saved is already the same as
            // the data in the model, but we can't (no IAnswerData.equals())
            model.getForm().setValue(data, index.getReference(), element, midSurvey);
            return true;
        } else {
            return false;
        }
    }


    /**
     * Navigates forward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    public int stepToNextEvent() {
        return stepEvent(true);
    }


    /**
     * Navigates backward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    public int stepToPreviousEvent() {
        return stepEvent(false);
    }


    /**
     * Moves the current FormIndex to the next/previous relevant position.
     *
     * @param forward
     * @return
     */
    private int stepEvent(boolean forward) {
        FormIndex index = model.getFormIndex();

        do {
            if (forward) {
                index = model.incrementIndex(index);
            } else {
                index = model.decrementIndex(index);
            }
        } while (index.isInForm() && !model.isIndexRelevant(index));

        return jumpToIndex(index);
    }


    /**
     * Jumps to a given FormIndex.
     *
     * @param index
     * @return EVENT for the specified Index.
     */
    public int jumpToIndex(FormIndex index) {
        model.setQuestionIndex(index);
        return model.getEvent(index);
    }

    public FormIndex descendIntoRepeat (int n) {
		jumpToIndex(model.getForm().descendIntoRepeat(model.getFormIndex(), n));
    	return model.getFormIndex();
    }

    public FormIndex descendIntoNewRepeat () {
		jumpToIndex(model.getForm().descendIntoRepeat(model.getFormIndex(), -1));
		newRepeat(model.getFormIndex());
		return model.getFormIndex();
    }

    /**
     * Creates a new repeated instance of the group referenced by the specified
     * FormIndex.
     *
     * @param questionIndex
     */
    public void newRepeat(FormIndex questionIndex) {
    	try{
    		model.getForm().createNewRepeat(questionIndex);
    	} catch(InvalidReferenceException ire) {
    		throw new RuntimeException("Invalid reference while copying itemset answer: " + ire.getMessage());
    	}
    }


    /**
     * Creates a new repeated instance of the group referenced by the current
     * FormIndex.
     *
     */
    public void newRepeat() {
        newRepeat(model.getFormIndex());
    }


    /**
     * Deletes a repeated instance of a group referenced by the specified
     * FormIndex.
     *
     * @param questionIndex
     * @return
     */
    public FormIndex deleteRepeat(FormIndex questionIndex) {
        return model.getForm().deleteRepeat(questionIndex);
    }


    /**
     * Deletes a repeated instance of a group referenced by the current
     * FormIndex.
     *
     * @return
     */
    public FormIndex deleteRepeat() {
        return deleteRepeat(model.getFormIndex());
    }

    public void deleteRepeat (int n) {
		deleteRepeat(model.getForm().descendIntoRepeat(model.getFormIndex(), n));
    }

    /**
     * Sets the current language.
     * @param language
     */
    public void setLanguage(String language) {
        model.setLanguage(language);
    }
}
