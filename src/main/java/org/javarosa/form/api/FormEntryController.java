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

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.condition.FilterStrategy;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to navigate through an xform and appropriately manipulate
 * the FormEntryModel's state.
 */
public class FormEntryController {
    private static final Logger logger = LoggerFactory.getLogger(FormEntryController.class);

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

    private final List<FormEntryFinalizationProcessor> formEntryFinalizationProcessors = new ArrayList<>();

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
     * Attempts to save the answer at the given {@link FormIndex} into the instance
     * and returns one of three possible {@code int} attempt result codes:
     * <ul>
     * <li>{@link #ANSWER_OK}
     * <li>{@link #ANSWER_REQUIRED_BUT_EMPTY}
     * <li>{@link #ANSWER_CONSTRAINT_VIOLATED}
     * </ul>
     * <p>
     * Side effects: When it returns {@link #ANSWER_OK}, it mutates
     * the {@link TreeElement} corresponding to the given {@link FormIndex} by
     * setting its value to the given {@link IAnswerData} or by copying an
     * itemset answer if the question is complex.
     *
     * @param index The index of the question/prompt that is being currently evaluated
     * @param data  The data to attempt to answer the question with.
     * @return the attempt's {@code int} result code
     * @throws RuntimeException when the question is complex and it has constraints.
     *                          See inline comments.
     * @see QuestionDef#isComplex()
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
                // TODO Design a test that exercizes this branch.
                model.getForm().copyItemsetAnswer(q, element, data);
            } catch (InvalidReferenceException ire) {
                logger.error("Error", ire);
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

    public void finalizeFormEntry() {
        model.getForm().postProcessInstance();
        formEntryFinalizationProcessors.stream().forEach(formEntryFinalizationProcessor -> {
            formEntryFinalizationProcessor.processForm(model);
        });
    }

    public void addPostProcessor(FormEntryFinalizationProcessor formEntryFinalizationProcessor) {
        formEntryFinalizationProcessors.add(formEntryFinalizationProcessor);
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

    public FormIndex descendIntoRepeat(int n) {
        jumpToIndex(model.getForm().descendIntoRepeat(model.getFormIndex(), n));
        return model.getFormIndex();
    }

    public FormIndex descendIntoNewRepeat() {
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
        try {
            model.getForm().createNewRepeat(questionIndex);
        } catch (InvalidReferenceException ire) {
            throw new RuntimeException("Invalid reference while copying itemset answer: " + ire.getMessage());
        }
    }


    /**
     * Creates a new repeated instance of the group referenced by the current
     * FormIndex.
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

    public void deleteRepeat(int n) {
        deleteRepeat(model.getForm().descendIntoRepeat(model.getFormIndex(), n));
    }

    /**
     * Sets the current language.
     *
     * @param language
     */
    public void setLanguage(String language) {
        model.setLanguage(language);
    }

    /**
     * Jump to the prompt to add a new repeat for the repeat the controller is currently in. If the current
     * position in the form is not in a repeat nothing will happen.
     */
    public void jumpToNewRepeatPrompt() {
        FormIndex repeatIndex = getRepeatGroupIndex(getModel().getFormIndex(), getModel().getForm());
        if (repeatIndex == null) {
            return;
        }

        int repeatDepth = repeatIndex.getDepth();

        do {
            stepToNextEvent();
        } while (getModel().getEvent() != EVENT_PROMPT_NEW_REPEAT
            || getModel().getFormIndex().getDepth() != repeatDepth);
    }

    private static FormIndex getRepeatGroupIndex(FormIndex index, FormDef formDef) {
        IFormElement element = formDef.getChild(index);
        if (element instanceof GroupDef && ((GroupDef) element).getRepeat()) {
            return index;
        } else {
            if (index.getPreviousLevel() != null) {
                return getRepeatGroupIndex(index.getPreviousLevel(), formDef);
            } else {
                return null;
            }
        }
    }

    public void disablePredicateCaching() {
        model.getForm().disablePredicateCaching();
    }

    public void addFilterStrategy(FilterStrategy filterStrategy) {
        model.getForm().addFilterStrategy(filterStrategy);
    }
}
