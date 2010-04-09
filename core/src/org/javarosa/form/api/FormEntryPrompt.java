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
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.NoLocalizedTextException;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.formmanager.view.IQuestionWidget;



/**
 * This class gives you all the information you need to display a question when
 * your current FormIndex references a QuestionEvent.
 * 
 * @author Yaw Anokwa
 */
public class FormEntryPrompt extends FormEntryCaption {

    TreeElement mTreeElement;

    /**
     * This empty constructor exists for convenience of any supertypes of this prompt
     */
    protected FormEntryPrompt() {
    }
    
    /**
	 * Creates a FormEntryPrompt for the element at the given index in the form.
	 * 
	 * @param form
	 * @param index
	 */
    public FormEntryPrompt(FormDef form, FormIndex index) {
        super(form, index);
        if (!(element instanceof QuestionDef))
        	throw new IllegalArgumentException("FormEntryPrompt can only be created for QuestionDef elements");
        this.mTreeElement = form.getInstance().resolveReference(index.getReference());
    }

    public int getControlType() {
        return getQuestion().getControlType();
    }

    public int getDataType() {
        return mTreeElement.dataType;
    }

    // attributes available in the bind, instance and body
    public String getPromptAttributes() {
        // TODO: implement me.
        return null;
    }

    public IAnswerData getAnswerValue() {
        return mTreeElement.getValue();
    }

    
    //TODO RE-ROUTE me through Localizer
    public String getAnswerText() {
        return mTreeElement.getValue().getDisplayText();
    }

    public String getConstraintText() {
        return mTreeElement.getConstraint().constraintMsg;
    }

    public Vector<SelectChoice> getSelectChoices() {
        return getQuestion().getChoices();
    }

//    /**
//     * Get a vector containing the captions (or URIs as the case may be)
//     * of the SelectChoices within the current Question.
//     * @param subForm Specificy a specific type of subform (e.g. "audio","image","long",etc) to return. Can be null. See getChoiceCaption(int,String) for specifics.
//     * @return
//     */
//    public Vector<String> getChoiceCaptions(String subForm){
//    	Vector<SelectChoice> choices = getSelectChoices();
//    	Vector<String> captions = new Vector<String>();
//    	
//    	for(int i=0;i<choices.size();i++){
//    		captions.addElement(getChoiceCaption(i,subForm));
//    	}
//    	
//    	return captions;
//    	
//    }
//    
//    /**
//     * Gets the caption of a select choice, according to form.
//     * if form=null or the subform doesn't exist for the current locale
//     * the long-form will be returned. If the long form doesn't exist either
//     * return the &ltvalue&gt innerText value.
//     * @param ind
//     * @param subform subform type (e.g. "long","audio","image","short",etc). Can be null.
//     * @return
//     */
//    public String getChoiceCaption(int ind,String subform){
//    	SelectChoice choice = getSelectChoices().elementAt(ind);
//    	Localizer l = form.getLocalizer();
//    	String caption = l.getText(choice.getCaptionID()+";"+subform,l.getLocale());
//    	if(caption==null && subform!="long") caption=l.getText(choice.getCaptionID()+";long");
//    
//    	if(caption==null) caption=choice.getValue();
//    	return caption;
//    }
    
    public boolean isRequired() {
        return mTreeElement.required;
    }

    public boolean isReadOnly() {
        return !mTreeElement.isEnabled();
    }
    
    public QuestionDef getQuestion() {
    	return (QuestionDef)element;
    }
    
    //==== observer pattern ====//
    
	public void register (IQuestionWidget viewWidget) {
		super.register(viewWidget);
		mTreeElement.registerStateObserver(this);
	}

	public void unregister () {
		mTreeElement.unregisterStateObserver(this);
		super.unregister();
	}
		
	public void formElementStateChanged(TreeElement instanceNode, int changeFlags) {
		if (this.mTreeElement != instanceNode)
			throw new IllegalStateException("Widget received event from foreign question");
		if (viewWidget != null)
			viewWidget.refreshWidget(changeFlags);		
	}
	
	/**
	 * ONLY RELEVANT to Question elements!
	 * Will throw runTimeException if this is called for anything that isn't a Question.
	 * @return
	 */
	public String getHelpText(){
		String helpText=null;
		try{
			helpText=form.getLocalizer().getLocalizedText(((QuestionDef)element).getHelpTextID());
		}catch(NoLocalizedTextException nlt){
			helpText = ((QuestionDef)element).getHelpText();
		}catch(UnregisteredLocaleException ule){
			System.err.println("Warning: No Locale set yet (while attempting to getHelpText())");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return helpText;
		
	}
}
