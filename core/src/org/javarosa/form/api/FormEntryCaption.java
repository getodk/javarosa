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
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.NoLocalizedTextException;
import org.javarosa.formmanager.view.IQuestionWidget;
import java.lang.String;
import java.util.Vector;

/**
 * This class gives you all the information you need to display a caption when
 * your current FormIndex references a GroupEvent, RepeatPromptEvent, or
 * RepeatEvent.
 * 
 * @author Simon Kelly
 */
public class FormEntryCaption implements FormElementStateListener {

	FormDef form;
	FormIndex index;
	protected IFormElement element;
	private String textID;
	
	public static final String TEXT_FORM_LONG = "long";
	public static final String TEXT_FORM_SHORT = "short";
	public static final String TEXT_FORM_AUDIO = "audio";
	public static final String TEXT_FORM_IMAGE = "image";
	
	protected String[] richMediaFormTypes = {TEXT_FORM_LONG,
										   TEXT_FORM_SHORT,
										   TEXT_FORM_AUDIO,
										   TEXT_FORM_IMAGE};

	protected IQuestionWidget viewWidget;

	/**
	 * This empty constructor exists for convenience of any supertypes of this
	 * prompt
	 */
	public FormEntryCaption() {
	}

	/**
	 * Creates a FormEntryCaption for the element at the given index in the form.
	 * 
	 * @param form
	 * @param index
	 */
	public FormEntryCaption(FormDef form, FormIndex index) {
		this.form = form;
		this.index = index;
		this.element = form.getChild(index);
		this.viewWidget = null;
		this.textID = this.element.getTextID();
	}

	/**
	 * Find out what Text forms (e.g. audio, long form text, etc) are available
	 * for this element or the element specified by textID
	 * @param textID (Optional) if null, uses textID of this FormEntryCaption
	 * @return String Array of form names available in current locale
	 */
	public Vector getAvailableTextForms() {
		return this.getAvailableTextForms(this.textID);
	}
	
	protected Vector getAvailableTextForms(String textID){
		String tID = textID;
		if(tID == null || tID == "") tID = this.textID; //fallback to this FormEntry's textID
		if(tID == null) return new Vector();
		String types="";

		//check for default
		if(null != localizer().getRawText(localizer().getLocale(), tID)){
			types+="default";
		}
		
		//run through types list
		for(int i=0;i<richMediaFormTypes.length;i++){
			String curType = richMediaFormTypes[i];
			if(null != localizer().getRawText(localizer().getLocale(), tID+";"+curType)){
				types+=","+curType;
			}
		}
		Vector vec = DateUtils.split(types,",",false);
		vec.removeElement("");
		return vec;
	}
	
	/**
	 * @return The default text form for this caption, either the inner
	 * text or the appropriate localized form.
	 */
	public String getDefaultText(){
		return getDefaultText(this.textID);
	}
	
	/**
	 * Get the default text for the provided ID; 
	 * @param textID 
	 * @return
	 */
	protected String getDefaultText(String textID){
		return getText(textID,null);
	}
	
	protected String getFormOrDefault(String textID, String form) {
		String t = null;
		if(textID == null) {
			return this.getText(null, null);
		}
		try{
			t = getText(textID,form);
		}catch(NoLocalizedTextException nlte){
			System.out.println("Warning, " + form + " text form requested for ["+textID+"] but doesn't exist. (Falling back to Default form).");
			t = getDefaultText(textID);
		}catch(IllegalArgumentException iae){
			System.out.println("Warning, Long text form requested for ["+textID+"] but doesn't exist. (Falling back to Default form).");
			t = getDefaultText(textID);
		}
		return t;

	}
	
	protected String getFormOrNull(String textID, String form) {
		if(textID==null)textID=this.textID;
		if(!getAvailableTextForms(textID).contains(form)){
			System.out.println("Warning: " + form +" text form requested for ["+textID+"] but it doesn't exist! Null returned.");
			return null;
		}
		return getText(textID,form);
	}
	
	/**
	 * Convenience method
	 * Get longText form of text for THIS element (if available) 
	 * Falls back to default if long text form doesn't exist.
	 * @return longText form 
	 */
	public String getLongText(){
		return getFormOrDefault(getTextID(), TEXT_FORM_LONG);
	}
	
	/**
	 * Convenience method
			System.out.println("Warning, Short text form requested for ["+textID+"] but doesn't exist. (Falling back to Default form).");
			t = getDefaultText(textID);
		}catch(IllegalArgumentException iae){
	 * Get shortText form of text for THIS element (if available) 
	 * @return shortText form 
	 */
	public String getShortText(){
		return getFormOrDefault(getTextID(), TEXT_FORM_SHORT);
	}
	
	/**
	 * Convenience method
	 * Get audio URI from Text form for THIS element (if available)
	 * @return audio URI form stored in current locale of Text, returns null if not available
	 */
	public String getAudioText() {
		return getFormOrNull(getTextID(), TEXT_FORM_AUDIO);
	}
	
	/**
	 * Convenience method
	 * Get image URI form of text for THIS element (if available)
	 * @return URI of image form stored in current locale of Text, returns null if not available
	 */
	public String getImageText() {
		return getFormOrNull(getTextID(), TEXT_FORM_IMAGE);
	}
	
	/**
	 * Standard Localized text retreiver.
	 * 
	 * use getAvailableTextForms to check which forms are available before you
	 * call this method.
	 * Falls back to labelInnerText if textID and form are null
	 * or if textID!=null and there is no Localized text available.
	 * 
	 * 
	 * @param tID
	 * @param form
	 * @return
	 * @throws IllegalArgumentException if this element is unlocalized but a special form is requested.
	 * 
	 * 
	 * 
	 */
	protected String getText(String tID,String form){
		if(form == "") form = null; //
		if(tID == "") tID = null;   //this is just to make the code look a little cleaner
	
		String text=null;		
		String textID = tID;

		if(textID == null){ //if no textID was specified as an argument...
			textID = this.textID; //switch to this FormEntry's ID.
			if(textID == null && form == null){ //If there still is no ID (ie it's not a localizable element)
				String tt = element.getLabelInnerText(); //get the inner text if available.		
				if(tt == null) return null;
				else return substituteStringArgs(tt);  //process any arguments in the text and return. 
				
			}else if(textID == null && form != null){ //But if it's not localized and you specified a form...
				throw new IllegalArgumentException("Can't ask for a special form for unlocalized element! Form = "+form);
			}
		}
		
		if(form!=null){
			textID += ";" + form;	
		}
		
		text = localizer().getLocalizedText(textID);
		return substituteStringArgs(text);
	}

	public String getAppearanceHint ()  {
		return element.getAppearanceAttr();
	}
	
	protected String substituteStringArgs(String templateStr) {
		if (templateStr == null) {
			return null;
		}
		return form.fillTemplateString(templateStr, index.getReference());
	}

	public int getMultiplicity() {
		return index.getElementMultiplicity();
	}

	public IFormElement getFormElement() {
		return element;
	}

	/**
	 * @return true if this represents a <repeat> element
	 */
	public boolean repeats() {
		if (element instanceof GroupDef) {
			return ((GroupDef) element).getRepeat();
		} else {
			return false;
		}
	}

	public FormIndex getIndex() {
		return index;
	}
	
	protected Localizer localizer() {
		return this.form.getLocalizer();
	}

	// ==== observer pattern ====//

	public void register(IQuestionWidget viewWidget) {
		this.viewWidget = viewWidget;
		element.registerStateObserver(this);
	}

	public void unregister() {
		this.viewWidget = null;
		element.unregisterStateObserver(this);
	}

	public void formElementStateChanged(IFormElement element, int changeFlags) {
		if (this.element != element)
			throw new IllegalStateException(
					"Widget received event from foreign question");
		if (viewWidget != null)
			viewWidget.refreshWidget(changeFlags);
	}

	public void formElementStateChanged(TreeElement instanceNode,
			int changeFlags) {
		throw new RuntimeException("cannot happen");
	}
	
	protected String getTextID(){
		return this.textID;
	}
	

	


}
