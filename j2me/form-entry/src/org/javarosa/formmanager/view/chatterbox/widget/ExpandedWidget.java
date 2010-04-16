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

package org.javarosa.formmanager.view.chatterbox.widget;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Image;

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public abstract class ExpandedWidget implements IWidgetStyleEditable {

	private StringItem prompt;
	protected Item entryWidget;
	private Container c;

	public ExpandedWidget () {
		reset();
	}

	public void initWidget (FormEntryPrompt fep, Container c) {
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		prompt = new StringItem(null, null);
		entryWidget = getEntryWidget(fep);
		//#style textBox
		UiAccess.setStyle(entryWidget);
		
		c.add(prompt);
		c.add(entryWidget);
		
		this.c = c;
	}
		
	public ImageItem getImageItem(FormEntryPrompt fep){
		Vector AvailFormTypes = fep.getAvailableTextFormTypes(fep.getTextID());
		Vector AvailForms = fep.getAllTextForms(fep.getTextID());
		String ILabel,IaltText;
		
		if(AvailFormTypes.contains("long")){
			ILabel = (String)AvailForms.elementAt(AvailFormTypes.indexOf("long"));
		}else{
			ILabel = fep.getDefaultText();
		}
		
		if(AvailFormTypes.contains("short")){
			IaltText = (String)AvailForms.elementAt(AvailFormTypes.indexOf("short"));
		}else{
			IaltText = fep.getDefaultText();
		}
		Image im = getImage(fep,null);
		if(im!=null){
			ImageItem imItem = new ImageItem(ILabel,getImage(fep,null), ImageItem.LAYOUT_CENTER, IaltText);
			return imItem;
		}else{
			return null;
		}
	}
	
	public Image getImage(FormEntryPrompt fep,SelectChoice sc){
		String Iuri;
		Vector AvailFormTypes;
		Vector AvailForms;
		Image im = null;
		
		if(sc!=null){
			AvailFormTypes = fep.getAvailSelectTextFormTypes(sc);
			AvailForms = fep.getAllSelectTextForms(sc);
		}else{
			AvailFormTypes = fep.getAvailableTextFormTypes(fep.getTextID());
			AvailForms = fep.getAllTextForms(fep.getTextID());
		}
		
		if(AvailFormTypes.contains("image")){
			Iuri = (String)AvailForms.elementAt(AvailFormTypes.indexOf("image"));
		}else{
			Iuri = null;
		}
		
		if(Iuri != null){
			try {
				im = Image.createImage(Iuri);
			} catch (IOException e) {
				throw new RuntimeException("ERROR! Cant find image at URI:"+Iuri);	
			}
		}
		
		return im;
	}

	public void refreshWidget (FormEntryPrompt fep, int changeFlags) {
		
		ImageItem imItem = getImageItem(fep);
		if(imItem!=null) c.add(imItem);
		
		String caption;
		
		if(fep.getAvailableTextFormTypes(fep.getTextID()).contains("long")){
			caption = fep.getLongText();
		}else if(fep.getAvailableTextFormTypes(fep.getTextID()).contains("short")){
			caption = fep.getShortText();
		}else{
			caption = fep.getDefaultText();
		}
		
		prompt.setText(caption);
		updateWidget(fep);
		
		//don't wipe out user-entered data, even on data-changed event
		IAnswerData data = fep.getAnswerValue();
		if (data != null && changeFlags == FormElementStateListener.CHANGE_INIT) {
			setWidgetValue(data.getValue());
		}
	}

	public IAnswerData getData () {
		return getWidgetValue();
	}

	public void reset () {
		prompt = null;
		entryWidget = null;
	}

	public boolean focus () {
		//do nothing special
		return false;
	}
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_MANUAL;
	}
	
	public Item getInteractiveWidget () {
		return entryWidget;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#getPinnableHeight()
	 */
	public int getPinnableHeight() {
		return prompt.getContentHeight();
	}
	
	protected abstract Item getEntryWidget (FormEntryPrompt prompt);
	protected abstract void updateWidget (FormEntryPrompt prompt);
	protected abstract void setWidgetValue (Object o);
	protected abstract IAnswerData getWidgetValue ();
}
