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
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;

import de.enough.polish.multimedia.AudioPlayer;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public abstract class ExpandedWidget implements IWidgetStyleEditable {

	private StringItem prompt;
	protected Item entryWidget;
	private Container c;
	private Container fullPrompt;
	private boolean IMAGE_DEBUG_MODE = false;

	public ExpandedWidget () {
		reset();
	}

	public void initWidget (FormEntryPrompt fep, Container c) {
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		fullPrompt= new Container(false);
		
		//#style prompttext
		prompt = new StringItem(null, null);
		fullPrompt.add(prompt);
		
		entryWidget = getEntryWidget(fep);
		//#style textBox
		UiAccess.setStyle(entryWidget);
		
		c.add(fullPrompt);
		c.add(entryWidget);
		
		this.c = c;
	}
	
	public ImageItem getImageItem(FormEntryPrompt fep){
//		Vector AvailForms = fep.getAvailableTextForms();
		String IaltText;

		IaltText = fep.getShortText();
		
		Image im = getImage(fep.getImageText());
		if(im!=null){
			ImageItem imItem = new ImageItem(null,getImage(fep.getImageText()), ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER, IaltText);
			imItem.setLayout(Item.LAYOUT_CENTER);
			return imItem;
		}else{
			return null;
		}
		
	}
	
	public Image getImage(String URI){
		if(URI != null){
			try {
				Reference ref = ReferenceManager._().DeriveReference(URI);
				InputStream is = ref.getStream();
				Image i = Image.createImage(is);
				is.close();
				return i;
			} catch (IOException e) {
				System.out.println("IOException for URI:"+URI);
				e.printStackTrace();
				if(IMAGE_DEBUG_MODE) throw new RuntimeException("ERROR! Cant find image at URI: "+URI);	
				return null;
			} catch (InvalidReferenceException ire){
				System.out.println("Invalid Reference Exception for URI:"+URI);
				ire.printStackTrace();
				if(IMAGE_DEBUG_MODE) throw new RuntimeException("Invalid Reference for image at: " +URI);
				return null;
			}
		} else{
			return null;
		}
	}

	private int imageIndex=-1;
	
	private ImageItem imItem;
	
	public void refreshWidget (FormEntryPrompt fep, int changeFlags) {
		if(imItem!=null && imageIndex !=-1){	//replace an already existing image
			imItem = getImageItem(fep);
			if(imItem!=null) fullPrompt.set(imageIndex, imItem);
			imageIndex = fullPrompt.indexOf(imItem);
		}else{
			imItem = getImageItem(fep);
			if(imItem!=null) fullPrompt.add(imItem);
			imageIndex = fullPrompt.indexOf(imItem);
		}
		
		Chatterbox.getAudioAndPlay(fep);
		prompt.setText(fep.getLongText());	
		updateWidget(fep);
		
		//don't wipe out user-entered data, even on data-changed event
		IAnswerData data = fep.getAnswerValue();
		if (data != null && changeFlags == FormElementStateListener.CHANGE_INIT) {
			setWidgetValue(cast(data).getValue());
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
	
	protected IAnswerData cast(IAnswerData data) {
		if(data instanceof UncastData) {
			return getAnswerTemplate().cast((UncastData)data);
		} else {
			return data;
		}
	}
	
	/**
	 * @return An IAnswerData object which defines a template for the
	 * data which should be given to this widget.
	 */
	protected abstract IAnswerData getAnswerTemplate();
	protected abstract Item getEntryWidget (FormEntryPrompt prompt);
	protected abstract void updateWidget (FormEntryPrompt prompt);
	protected abstract void setWidgetValue (Object o);
	protected abstract IAnswerData getWidgetValue ();
}
