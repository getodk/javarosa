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
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.DateUtils;
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
   
    public String getConstraintText() {
        return mTreeElement.getConstraint().constraintMsg;
    }

    public Vector<SelectChoice> getSelectChoices() {
        return getQuestion().getChoices();
    }


    
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
	
	public Localizer getLocalizer(){
		return this.localizer;
	}
	
	
	/**
	 * 
	 * @return localized Question text (default form), LabelInnerText if default form is not available.
	 */
	public String getQText(){
		return this.getDefaultText(null);
	}
	
	/**
	 * 
	 * @param form Specific subform of question text (e.g. "audio","image", etc)
	 * @return Question text subform (SEE Localizer.getLocalizedText(String) for fallback details). Null if form not available
	 */
	public String getQText(String form){
		return this.getText(this.getTextID(), form);
	}
	
	

	

	

	
	/**
	 * Get the text for the specified selection (localized if possible)
	 * @param sel the selection
	 * @return localized (if available, default LabelInnerText if not) text label.  If no localized version
	 * is available, will attempt to return labelInnerText. If not available throws NullPointerException.
	 * @throws NullPointerException
	 */
	public String getSelectionText(Selection sel){
		return getSelectChoiceText(sel.choice);
	}
	
	/**
	 * Get the text for the specified SelectChoice (localized if possible)
	 * @param sel the selection
	 * @return localized (if available, default LabelInnerText if not) text label.  If no localized version
	 * is available, will attempt to return labelInnerText. If not available throws NullPointerException.
	 * @throws NullPointerException
	 */
	public String getSelectChoiceText(SelectChoice sel){
		String tID = sel.getTextID();
		
		if(tID == null || tID == ""){
			return sel.getLabelInnerText();
		}
		
		String text = getLongText(tID);
		if(text == null || text == ""){
			text = sel.getLabelInnerText(); //final fallback
		}
		
		return text;
	}
	

	
	/**
	 * 
	 * @param sel
	 * @return String array of all the Itext form texts available for this text
	 */
	public Vector getAllSelectTextForms(SelectChoice sel){
		String tID = sel.getTextID();
		if(tID == null || tID == "") return new Vector();
		
		String texts = "";
		Vector availForms = getAvailSelectTextFormTypes(sel);
		
		for(int i=0;i<availForms.size();i++){
			String curForm = (String)availForms.elementAt(i);
			
			if(curForm == "default"){
				texts+=","+getText(tID,"");
				continue;
			}
			
			texts +=","+getText(tID,curForm);
		}
		
		Vector vec = DateUtils.split(texts,",",false);
		vec.removeElement("");
		return vec;
	}
	
	//sorry for the ugly wording...
	public Vector getAvailSelectTextFormTypes(SelectChoice sel){
		String tID = sel.getTextID();

		if(tID == null||tID=="") return new Vector();
		String types="";

		//check for default
		if(null != localizer.getRawText(localizer.getLocale(), tID)){
			types+="default";
		}
		
		//run through types list
		for(int i=0;i<richMediaFormTypes.length;i++){
			String curType = richMediaFormTypes[i];
			if(null != localizer.getRawText(localizer.getLocale(), tID+";"+curType)){
				types+=","+curType;
			}
		}
		Vector vec = DateUtils.split(types,",",false);
		vec.removeElement("");
		return vec;
	}
		
	
	
	/**
	 * Get the Itext for a specific selection and specific itext form
	 * @param s
	 * @param form
	 * @return
	 */
	public String getSelectText(Selection s,String form){
		return getText(s.choice.getTextID(), form);
	}
	
	/**
	 * Get the Itext for a specific SelectChoice and specific itext form
	 * @param s
	 * @param form
	 * @return
	 */
	public String getSelectChoiceText(SelectChoice sel, String form){
		return getText(sel.getTextID(), form);
	}
}

