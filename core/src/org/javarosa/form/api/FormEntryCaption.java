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
	private Localizer localizer;
	
	String[] richMediaFormTypes = {"long",
								   "short",
								   "audio",
								   "image"};

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
		this.localizer = this.form.getLocalizer();
	}

	/**
	 * Find out what RichMediaTypes forms (e.g. audio, long form text, etc) are available
	 * for the element
	 * @param textID
	 * @return String Array of form names available in current locale
	 */
	public Vector getAvailableRMForms(){
		if(textID==null)return new Vector();
		String types="";
		//String[] types= new String[richMediaFormTypes.length];
		for(int i=0;i<richMediaFormTypes.length;i++){
			String curType = richMediaFormTypes[i];
			if(null != localizer.getRawText(localizer.getLocale(), textID+";"+curType)){
				types+=","+curType;
			}
		}
		return DateUtils.split(types,",",false);
	}
	
	public String getDefaultText(){
		if (textID == null) return null;
		System.out.print("getDefaultText()...");
		String txt;
		try{
			txt = localizer.getLocalizedText(textID);
			System.out.println("result:"+txt+" ");
		}catch(NoLocalizedTextException nlte){
			System.out.println("No Localized Text Exception!");
			txt = element.getLabelInnerText();
		}
		
		return txt;  
	}
	
	/**
	 * Convenience method
	 * Get longText form of text (if available) (falls back to default form (&lttext&gt with no form="something") then falls back to unlocalized version (innertext in actual label)
	 * @return longText form 
	 */
	public String getLongText() {
		if(textID==null)return null;
		
		String t = getRichMediaText("long");
		if(t==null)t=getDefaultText();
		return substituteStringArgs(t);
	}

	/**
	 * Convenience method
	 * Get shortText form of text (if available) (falls back to default form (&lttext&gt with no form="something") then falls back to unlocalized version (innertext in actual label)
	 * @return shortText form 
	 */
	public String getShortText() {
		if(textID==null)return null;
		
		String t = getRichMediaText("short");
		if(t==null)t=getDefaultText();
		return substituteStringArgs(t);
	}
	
	/**
	 * Convenience method
	 * Get audio URI from Text form (if available)
	 * @return audio URI form stored in current locale of Text, returns null if not available
	 */
	public String getAudioURI() {
		if(textID==null)return null;
		
		return substituteStringArgs(getRichMediaText("audio"));
	}
	
	/**
	 * Convenience method
	 * Get image URI form of text (if available)
	 * @return URI of image form stored in current locale of Text, returns null if not available
	 */
	public String getImageURI() {
		if(textID==null)return null;
		
		return substituteStringArgs(getRichMediaText("image"));
	}
	
	/**
	 * Retrieves the text (or value or resource URI) of the
	 * element (for the current locale) according to the form specificed (e.g. "long","audio",etc)
	 * @param form type of media (&ltvalue form="..."$gt)
	 * @return The actual value string (usually text or a URI) for that form, or the default value if the special form doesn't exist.
	 */
	public String getRichMediaText(String form){
		
		/////////######
		System.out.println("getRichMediaText("+form+") calls localizer.getRawText("+textID+";"+form+")");
		System.out.println("textID="+textID+",form="+form);
		/////////######
		
		
		if(textID==null) return null;
		if(form==null) return getDefaultText();
		
		
		
		if(getAvailableRMForms().contains(form)){
			return localizer.getRawText(localizer.getLocale(),textID+";"+form);
		}else{
			return null;
		}
	}
	
	/**
	 * Convenience method.
	 * 
	 * @return A String array of all the texts/URIs of all the available special localized forms for this element's itext. (Given in the order provided by getAvailableRMForms())
	 */ 
	public Vector getAllRMTexts(){
		String forms = "";
		for(int i=0;i<getAvailableRMForms().size();i++){
			forms +=","+getRichMediaText((String)getAvailableRMForms().elementAt(i));
		}
		return DateUtils.split(forms,",",false);
	}

	public String getAppearanceHint ()  {
		return element.getAppearanceAttr();
	}
	
	public String substituteStringArgs(String templateStr) {
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
	

	


}
