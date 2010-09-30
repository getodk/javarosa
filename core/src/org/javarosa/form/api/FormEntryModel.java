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
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

/**
 * The data model used during form entry. Represents the current state of the
 * form and provides access to the objects required by the view and the
 * controller.
 */
public class FormEntryModel {
    private FormDef form;
    private FormIndex currentFormIndex;


    public FormEntryModel(FormDef form) {
        this.form = form;
        this.currentFormIndex = FormIndex.createBeginningOfFormIndex();
    }


    /**
     * Given a FormIndex, returns the event this FormIndex represents.
     * 
     * @see FormEntryController
     */
    public int getEvent(FormIndex index) {
        if (index.isBeginningOfFormIndex()) {
            return FormEntryController.EVENT_BEGINNING_OF_FORM;
        } else if (index.isEndOfFormIndex()) {
            return FormEntryController.EVENT_END_OF_FORM;
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
                if (!FormIndex.NONLINEAR_REPEAT_API && form.getInstance().resolveReference(form.getChildInstanceRef(index)) == null) {
                    return FormEntryController.EVENT_PROMPT_NEW_REPEAT;
                } else if (FormIndex.NONLINEAR_REPEAT_API && index.getElementMultiplicity() == TreeReference.INDEX_REPEAT_JUNCTURE) {
                	return FormEntryController.EVENT_REPEAT_JUNCTURE;
                } else {
                    return FormEntryController.EVENT_REPEAT;
                }
            } else {
                return FormEntryController.EVENT_GROUP;
            }
        } else {
            return FormEntryController.EVENT_QUESTION;
        }
    }
    
    /**
     * 
     * @param index
     * @return
     */
    protected TreeElement getTreeElement(FormIndex index) {
        return form.getInstance().resolveReference(index.getReference());
    }


    /**
     * @return the event for the current FormIndex
     * @see FormEntryController
     */
    public int getEvent() {
        return getEvent(currentFormIndex);
    }


    /**
     * @return Form title
     */
    public String getFormTitle() {
        return form.getTitle();
    }


    /**
     * 
     * @param index
     * @return Returns the FormEntryPrompt for the specified FormIndex if the
     *         index represents a question.
     */
    public FormEntryPrompt getQuestionPrompt(FormIndex index) {
        if (form.getChild(index) instanceof QuestionDef) {
            return new FormEntryPrompt(form, index);
        } else {
            throw new RuntimeException(
                    "Invalid query for Question prompt. Non-Question object at the form index");
        }
    }


    /**
     * 
     * @param index
     * @return Returns the FormEntryPrompt for the current FormIndex if the
     *         index represents a question.
     */
    public FormEntryPrompt getQuestionPrompt() {
        return getQuestionPrompt(currentFormIndex);
    }


    /**
     * When you have a non-question event, a CaptionPrompt will have all the
     * information needed to display to the user.
     * 
     * @param index
     * @return Returns the FormEntryCaption for the given FormIndex if is not a
     *         question.
     */
    public FormEntryCaption getCaptionPrompt(FormIndex index) {
        return new FormEntryCaption(form, index);
    }



    /**
     * When you have a non-question event, a CaptionPrompt will have all the
     * information needed to display to the user.
     * 
     * @param index
     * @return Returns the FormEntryCaption for the current FormIndex if is not
     *         a question.
     */
    public FormEntryCaption getCaptionPrompt() {
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


    /**
     * Not yet implemented
     * 
     * Should get the number of completed questions to this point.
     */
    public int getCompletedRelevantQuestionCount() {
        // TODO: Implement me.
        return 0;
    }


    /**
     * Not yet implemented
     * 
     * Should get the total possible questions given the current path through the form.
     */
    public int getTotalRelevantQuestionCount() {
        // TODO: Implement me.
        return 0;
    }
    
    /**
     * @return total number of questions in the form, regardless of relevancy
     */
    public int getNumQuestions() {
        return form.getDeepChildCount();
    }


    /**
     * 
     * @return Returns the current FormIndex referenced by the FormEntryModel.
     */
    public FormIndex getFormIndex() {
        return currentFormIndex;
    }


    protected void setLanguage(String language) {
        if (form.getLocalizer() != null) {
            form.getLocalizer().setLocale(language);
        }
    }


    /**
     * 
     * @return Returns the currently selected language.
     */
    public String getLanguage() {
        return form.getLocalizer().getLocale();
    }


    /**
     * Set the FormIndex for the current question.
     * 
     * @param index
     */
    public void setQuestionIndex(FormIndex index) {
        if (!currentFormIndex.equals(index)) {
            // See if a hint exists that says we should have a model for this
            // already
            createModelIfNecessary(index);
            currentFormIndex = index;
        }
    }


    /**
     * 
     * @return
     */
    public FormDef getForm() {
        return form;
    }


    /**
     * Returns a hierarchical list of FormEntryCaption objects for the given
     * FormIndex
     * 
     * @param index
     * @return list of FormEntryCaptions in hierarchical order
     */
    public FormEntryCaption[] getCaptionHierarchy(FormIndex index) {
        Vector<FormEntryCaption> captions = new Vector<FormEntryCaption>();
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
     * Returns a hierarchical list of FormEntryCaption objects for the current
     * FormIndex
     * 
     * @param index
     * @return list of FormEntryCaptions in hierarchical order
     */
    public FormEntryCaption[] getCaptionHierarchy() {
        return getCaptionHierarchy(currentFormIndex);
    }


    /**
     * @param index
     * @return true if the element at the specified index is read only
     */
    public boolean isIndexReadonly(FormIndex index) {
        if (index.isBeginningOfFormIndex() || index.isEndOfFormIndex())
            return true;
        
        TreeReference ref = form.getChildInstanceRef(index);
        boolean isAskNewRepeat = (getEvent(index) == FormEntryController.EVENT_PROMPT_NEW_REPEAT ||
        						  getEvent(index) == FormEntryController.EVENT_REPEAT_JUNCTURE);

        if (isAskNewRepeat) {
            return false;
        } else {
            TreeElement node = form.getInstance().resolveReference(ref);
            return !node.isEnabled();
        }
    }


    /**
     * @param index
     * @return true if the element at the current index is read only
     */
    public boolean isIndexReadonly() {
        return isIndexReadonly(currentFormIndex);
    }


    /**
     * Determine if the current FormIndex is relevant. Only relevant indexes
     * should be returned when filling out a form.
     * 
     * @param index
     * @return true if current element at FormIndex is relevant
     */
    public boolean isIndexRelevant(FormIndex index) {
        TreeReference ref = form.getChildInstanceRef(index);
        boolean isAskNewRepeat = (getEvent(index) == FormEntryController.EVENT_PROMPT_NEW_REPEAT);
        boolean isRepeatJuncture = (getEvent(index) == FormEntryController.EVENT_REPEAT_JUNCTURE);
        
        boolean relevant;
        if (isAskNewRepeat) {
            relevant = form.isRepeatRelevant(ref) && form.canCreateRepeat(ref);
            //repeat junctures are still relevant if no new repeat can be created; that option
            //is simply missing from the menu
        } else if (isRepeatJuncture) {
        	relevant = form.isRepeatRelevant(ref);
        } else {
            TreeElement node = form.getInstance().resolveReference(ref);
            relevant = node.isRelevant(); // check instance flag first
        }

        if (relevant) { // if instance flag/condition says relevant, we still
            // have to check the <group>/<repeat> hierarchy

            FormIndex ancestorIndex = index;
            while (!ancestorIndex.isTerminal()) {
                // This should be safe now that the TreeReference is contained
                // in the ancestor index itself
                TreeElement ancestorNode =
                        form.getInstance().resolveReference(ancestorIndex.getLocalReference());

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
     * Determine if the current FormIndex is relevant. Only relevant indexes
     * should be returned when filling out a form.
     * 
     * @param index
     * @return true if current element at FormIndex is relevant
     */
    public boolean isIndexRelevant() {
        return isIndexRelevant(currentFormIndex);
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
                    IAnswerData count = getForm().getInstance().getDataValue(g.getCountReference());
                    if (count != null) {
                        int fullcount = ((Integer) count.getValue()).intValue();
                        TreeReference ref = getForm().getChildInstanceRef(index);
                        TreeElement element = getForm().getInstance().resolveReference(ref);
                        if (element == null) {
                            if (index.getInstanceIndex() < fullcount) {
                            	
                                try {
									getForm().createNewRepeat(index);
								} catch (InvalidReferenceException ire) {
									ire.printStackTrace();
									throw new RuntimeException("Invalid Reference while creting new repeat!" + ire.getMessage());
								}
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    public boolean isIndexCompoundContainer() {
    	return isIndexCompoundContainer(getFormIndex());
    }
    
    public boolean isIndexCompoundContainer(FormIndex index) {
    	FormEntryCaption caption = getCaptionPrompt(index);
    	return getEvent(index) == FormEntryController.EVENT_GROUP && caption.getAppearanceHint() != null && caption.getAppearanceHint().toLowerCase().equals("full");
    }
    
    public boolean isIndexCompoundElement() {
    	return isIndexCompoundElement(getFormIndex());
    }
    
    public boolean isIndexCompoundElement(FormIndex index) {
    	//Can't be a subquestion if it's not even a question!
    	if(getEvent(index) != FormEntryController.EVENT_QUESTION) {
    		return false;
    	}
    	
    	//get the set of nested groups that this question is in.
    	FormEntryCaption[] captions = getCaptionHierarchy(index);
    	for(FormEntryCaption caption : captions) {
    		
    		//If one of this question's parents is a group, this question is inside of it.
    		if(isIndexCompoundContainer(caption.getIndex())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public FormIndex[] getCompoundIndices() {
    	return getCompoundIndices(getFormIndex());
    }
    
    public FormIndex[] getCompoundIndices(FormIndex container) {
    	//ArrayLists are a no-go for J2ME
    	Vector<FormIndex> indices = new Vector<FormIndex>();
    	FormIndex walker = getForm().incrementIndex(container);
    	while(FormIndex.isSubElement(container, walker)) {
    		if(isIndexRelevant(walker)) {
    			indices.addElement(walker);
    		}
    		walker = getForm().incrementIndex(walker);
    	}
    	FormIndex[] array = new FormIndex[indices.size()];
    	for(int i = 0 ; i < indices.size() ; ++i) {
    		array[i] = indices.elementAt(i);
    	}
    	return array;
    }
}
