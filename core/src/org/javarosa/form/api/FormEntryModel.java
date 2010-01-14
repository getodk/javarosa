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
    private FormIndex currentFormindex;

    // total number of questions in the form; used for progress bar
    public int totalQuestions; 

    // Start New stuff today.

    /**
     * Given a FormIndex, returns the event this formindex should display in a view.
     * 
     */
    public int getEvent(FormIndex index) {
        if (index.isBeginningOfFormIndex()) {
            return FormEntryController.BEGINNING_OF_FORM_EVENT;
        } else if (index.isEndOfFormIndex()) {
            return FormEntryController.END_OF_FORM_EVENT;
        }

        //This came from chatterbox, and is unclear how correct it is, commented out for now. 
        //DELETEME: If things work fine
        //Vector defs = form.explodeIndex(index);
        //IFormElement last = (defs.size() == 0 ? null : (IFormElement) defs.lastElement());
        IFormElement element = form.getChild(index);
        if (element instanceof GroupDef) {
            if (((GroupDef) element).getRepeat()) {
            	if(form.getDataModel().resolveReference(form.getChildInstanceRef(index)) == null){
            		return FormEntryController.PROMPT_NEW_REPEAT_EVENT;
            	}else {
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
	
    /* 
     * @return the event for the current FormIndex
     */
    public int getCurrentEvent() {
        return getEvent(currentFormindex);
	}

    /**
     * 
     * @return Form title
     */
    public String getFormTitle() {
        return form.getTitle();
    }

    
    public FormEntryPrompt getQuestionPrompt(FormIndex index) {
    	if(form.getChild(index) instanceof QuestionDef) {
    		return new FormEntryPrompt(form, index);
    	} else {
    		throw new RuntimeException("Invalid query for Question prompt. Non-Question object at the form index");
    	}
    }

    public FormEntryPrompt getQuestionPrompt() {
        return getQuestionPrompt(currentFormindex);
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
        // TODO
        return 0;
    }


    public int getTotalRelevantQuestionCount() {
        // TODO
        return 0;
    }


    public FormIndex getCurrentFormIndex() {
        return currentFormindex;
    } 
    
    // end new stuff



    protected void setCurrentLanguage(String language) {
    	if(form.getLocalizer() != null) {
    		form.getLocalizer().setLocale(language);
    	}
    }


    public void setQuestionIndex(FormIndex index) {
        if (!currentFormindex.equals(index)) {
            // See if a hint exists that says we should have a model for this already
            createModelIfNecessary(index);
            currentFormindex = index;
        }
    }

    
    public FormDef getForm() {
        return form;
    }
    
    public String[] getCaptionHeirarchy(FormIndex index) {
    	Vector tempStrings = new Vector();
    	while(index != null) {
    		//Get the new caption prompt
    		//String title = this.get(index);
    		//if(title != null) {
    		//	tempStrings.add(title);
    		//}
    	}
    	String[] output = new String[tempStrings.size()];
    	tempStrings.toArray(output);
    	return output;
    }

    
    /**
     * 
     * @return total number of questions in the form
     */
    public int getNumQuestions() {
        return form.getDeepChildCount();
    }

    public boolean isReadonly(FormIndex questionIndex) {
        TreeReference ref = form.getChildInstanceRef(questionIndex);
        boolean isAskNewRepeat = getEvent(questionIndex) == FormEntryController.PROMPT_NEW_REPEAT_EVENT;

        if (isAskNewRepeat) {
            return false;
        } else {
            TreeElement node = form.getDataModel().resolveReference(ref);
            return !node.isEnabled();
        }
    }


    public boolean isRelevant(FormIndex questionIndex) {
        TreeReference ref = form.getChildInstanceRef(questionIndex);
        boolean isAskNewRepeat = getEvent(questionIndex) == FormEntryController.PROMPT_NEW_REPEAT_EVENT;

        boolean relevant;
        if (isAskNewRepeat) {
            relevant = form.canCreateRepeat(ref);
        } else {
            TreeElement node = form.getDataModel().resolveReference(ref);
            relevant = node.isRelevant(); // check instance flag first
        }

        if (relevant) { // if instance flag/condition says relevant, we still
            // have to check the <group>/<repeat> hierarchy
            FormIndex ancestorIndex = questionIndex.getNextLevel();
            while(ancestorIndex != null) {
            	//This should be safe now that the TreeReference is contained in the ancestor index itself
                TreeElement ancestorNode = form.getDataModel().resolveReference(ancestorIndex.getReference());
                
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
