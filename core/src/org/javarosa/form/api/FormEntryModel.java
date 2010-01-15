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

import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

public class FormEntryModel {
    private FormDef form;
    private FormIndex currentFormIndex;


    public FormEntryModel(FormDef form) {
        this.form = form;
        this.currentFormIndex = FormIndex.createBeginningOfFormIndex();
    }


    /**
     * Given a FormIndex, returns the event this FormIndex should display in a
     * view.
     * 
     */
    public int getEvent(FormIndex index) {
        if (index.isBeginningOfFormIndex()) {
            return FormEntryController.BEGINNING_OF_FORM_EVENT;
        } else if (index.isEndOfFormIndex()) {
            return FormEntryController.END_OF_FORM_EVENT;
        }

        // This came from chatterbox, and is unclear how correct it is,
        // commented out for now.
        // DELETEME: If things work fine
        // Vector defs = form.explodeIndex(index);
        // IFormElement last = (defs.size() == 0 ? null : (IFormElement)
        // defs.lastElement());
        IFormElement element = form.getChild(index);
        if (element instanceof GroupDef) {
            if (((GroupDef) element).getRepeat()) {
                if (form.getDataModel().resolveReference(form.getChildInstanceRef(index)) == null) {
                    return FormEntryController.PROMPT_NEW_REPEAT_EVENT;
                } else {
                    return FormEntryController.REPEAT_EVENT;
                }
            } else {
                return FormEntryController.GROUP_EVENT;
            }
        } else {
            return FormEntryController.QUESTION_EVENT;
        }
    }


    protected TreeElement getTreeElement(FormIndex index) {
        return form.getDataModel().resolveReference(index.getReference());
    }


    /**
     * @return the event for the current FormIndex
     */
    public int getCurrentEvent() {
        return getEvent(currentFormIndex);
    }


    /**
     * @return Form title
     */
    public String getFormTitle() {
        return form.getTitle();
    }


    /**
     * When you have a question event, a QuestionPrompt will have all the
     * information needed to display to the user.
     * 
     * @param index
     * @return
     */
    public FormEntryPrompt getQuestionPrompt(FormIndex index) {
        if (form.getChild(index) instanceof QuestionDef) {
            return new FormEntryPrompt(form, index);
        } else {
            throw new RuntimeException(
                    "Invalid query for Question prompt. Non-Question object at the form index");
        }
    }


    public FormEntryPrompt getCurrentQuestionPrompt() {
        return getQuestionPrompt(currentFormIndex);
    }


    /**
     * When you have a non-question event, a CaptionPrompt will have all the
     * information needed to display to the user.
     * 
     * @param index
     * @return
     */
    public FormEntryCaption getCaptionPrompt(FormIndex index) {
        return new FormEntryCaption(form, index);
    }


    public FormEntryCaption getCurrentCaptionPrompt() {
        return getCaptionPrompt(currentFormIndex);
    }


    /**
     * 
     * @return an array of Strings of the current langauges. Null if there are
     *         none.
     */
    public String[] getLanguages() {
        if (form.getLocalizer() != null) {
            return form.getLocalizer().getAvailableLocales();
        }
        return null;
    }


    public int getCurrentRelevantQuestionCount() {
        // TODO: Implement me.
        return 0;
    }


    public int getTotalRelevantQuestionCount() {
        // TODO: Implement me.
        return 0;
    }


    public FormIndex getCurrentFormIndex() {
        return currentFormIndex;
    }


    protected void setCurrentLanguage(String language) {
        if (form.getLocalizer() != null) {
            form.getLocalizer().setLocale(language);
        }
    }


    public String getCurrentLanguage() {
        return form.getLocalizer().getLocale();
    }


    public void setQuestionIndex(FormIndex index) {
        if (!currentFormIndex.equals(index)) {
            // See if a hint exists that says we should have a model for this
            // already
            createModelIfNecessary(index);
            currentFormIndex = index;
        }
    }


    public FormDef getForm() {
        return form;
    }


    /**
     * Returns a hierarchical list of FormEntryCaption objects for the given
     * FormIndex
     * 
     * @param index
     * @return list of FormEntryCaptions, FormEntryCaption of current index
     *         first.
     */
    public FormEntryCaption[] getCaptionHierarchy(FormIndex index) {
        Vector captions = new Vector();
        FormIndex remaining = index;
        while (remaining != null) {
            remaining = remaining.getNextLevel();
            FormIndex localIndex = index.diff(remaining);
            IFormElement element = form.getChild(localIndex);
            if (element != null) {
                FormEntryCaption caption = null;
                if (element instanceof GroupDef)
                    caption = new FormEntryCaption(getForm(), localIndex);
                else if (element instanceof QuestionDef)
                    caption = new FormEntryPrompt(getForm(), localIndex);

                if (caption != null) {
                    captions.addElement(caption);
                }
            }
        }
        FormEntryCaption[] captionArray = new FormEntryCaption[captions.size()];
        captions.copyInto(captionArray);
        return captionArray;
    }


    /**
     * 
     * @return total number of questions in the form, regardless of relevancy
     */
    public int getNumQuestions() {
        return form.getDeepChildCount();
    }


    public boolean isReadonly(FormIndex questionIndex) {
        TreeReference ref = form.getChildInstanceRef(questionIndex);
        boolean isAskNewRepeat =
                getEvent(questionIndex) == FormEntryController.PROMPT_NEW_REPEAT_EVENT;

        if (isAskNewRepeat) {
            return false;
        } else {
            TreeElement node = form.getDataModel().resolveReference(ref);
            return !node.isEnabled();
        }
    }


    /**
     * Determine if the current FormIndex is relevant. Only relevant indexes
     * should be returned when filling out a form.
     * 
     * @param questionIndex
     * @return
     */
    public boolean isRelevant(FormIndex questionIndex) {
        TreeReference ref = form.getChildInstanceRef(questionIndex);
        boolean isAskNewRepeat =
                getEvent(questionIndex) == FormEntryController.PROMPT_NEW_REPEAT_EVENT;

        boolean relevant;
        if (isAskNewRepeat) {
            relevant = form.canCreateRepeat(ref);
        } else {
            TreeElement node = form.getDataModel().resolveReference(ref);
            relevant = node.isRelevant(); // check instance flag first
        }

        if (relevant) { // if instance flag/condition says relevant, we still
            // have to check the <group>/<repeat> hierarchy

            FormIndex ancestorIndex = questionIndex;
            while (!ancestorIndex.isTerminal()) {
                // This should be safe now that the TreeReference is contained
                // in the ancestor index itself
                TreeElement ancestorNode =
                        form.getDataModel().resolveReference(ancestorIndex.getLocalReference());

                if (!ancestorNode.isRelevant()) {
                    relevant = false;
                    break;
                }
                ancestorIndex = ancestorIndex.getNextLevel();
            }
        }

        return relevant;
    }


    /**
     * For the current index: Checks whether the index represents a node which
     * should exist given a non-interactive repeat, along with a count for that
     * repeat which is beneath the dynamic level specified.
     * 
     * If this index does represent such a node, the new model for the repeat is
     * created behind the scenes and the index for the initial question is
     * returned.
     * 
     * Note: This method will not prevent the addition of new repeat elements in
     * the interface, it will merely use the xforms repeat hint to create new
     * nodes that are assumed to exist
     * 
     * @param The index to be evaluated as to whether the underlying model is
     *        hinted to exist
     */
    private void createModelIfNecessary(FormIndex index) {
        if (index.isInForm()) {
            IFormElement e = getForm().getChild(index);
            if (e instanceof GroupDef) {
                GroupDef g = (GroupDef) e;
                if (g.getRepeat() && g.getCountReference() != null) {
                    IAnswerData count =
                            getForm().getDataModel().getDataValue(g.getCountReference());
                    if (count != null) {
                        int fullcount = ((Integer) count.getValue()).intValue();
                        TreeReference ref = getForm().getChildInstanceRef(index);
                        TreeElement element = getForm().getDataModel().resolveReference(ref);
                        if (element == null) {
                            if (index.getInstanceIndex() < fullcount) {
                                getForm().createNewRepeat(index);
                            }
                        }
                    }
                }
            }
        }
    }
}
