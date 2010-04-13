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

    
//    //TODO AnswerText is.. problematic.
//    //There are various forms of "AnswerText" that could be returned
//    //depending on what the answer type is.
//    public String getAnswerText() {
//        return getAnswerValue().getDisplayText();
//    }

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
	
	public Localizer getLocalizer(){
		return this.localizer;
	}
	
	
	/**
	 * 
	 * @return localized Question text (default form), LabelInnerText if default form is not available.
	 */
	public String getQText(){
		return this.getDefaultText();
	}
	
	/**
	 * 
	 * @param form Specific subform of question text (e.g. "audio","image", etc)
	 * @return Question text subform (SEE Localizer.getLocalizedText(String) for fallback details). Null if form not available
	 */
	public String getQText(String form){
		return this.getText(this.getTextID(), form);
	}
	
	

	
//	/**
//	 * 
//	 * @return String array of all available text forms for this question text
//	 * example {{"default","Text in Bla"},{"audio","sound-file.mp3"},{"image","image.jpg"}}
//	 * Forms are in no particular order.  If there are no forms to be found, an empty array will be returned.
//	 */
//	public String[][] getTextForms(){
//		return getTextForms(this.getTextID());
//	}
//	
//	private String[][] getTextForms(String tID){
//		Vector strings = new Vector();
//		Vector types = new Vector();
//		
//		String temp;
//		
//		String defaultText = this.localizer.getText(tID);
//		
//		if(defaultText!=null && defaultText != ""){
//			strings.addElement(defaultText);
//			types.addElement("default");
//		}
//		
//		for(String s:this.richMediaFormTypes){
//			temp = this.localizer.getText(tID+";"+s);
//			if(temp==defaultText || temp == null || temp == ""){
//				continue;
//			}else{
//				strings.addElement(temp);
//				types.addElement(s);
//			}
//			
//		}
//		
//		String returnStrings[][] = new String[types.size()][2];
//		for(int i = 0;i<types.size();i++){
//			returnStrings[i][0] = (String)types.elementAt(i);
//			returnStrings[i][1] = (String)strings.elementAt(i);
//		}
//		
//		return returnStrings;
//	}
	

	
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
		
		String text = getText(tID,"");
		if(text == null || text == ""){
			text = sel.getLabelInnerText(); //final fallback
		}
		
		return text;
	}
	

	
	/**
	 * 
	 * @param sel
	 * @return String array of all the Itext form types available for this text
	 * see getTextForms() javadoc for example.
	 */
	public String[][] getSelectTextForms(Selection sel){
		return getTextForms(sel.choice.getTextID());
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
	
	
//	/**
//	 * Get the answer text for this question.
//	 * @return
//	 * TODO
//	 */
//	public String getAnswerText(){
//		return null
//	}



}

