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

package org.javarosa.formmanager.view.widgets;


import java.io.IOException;

import javax.microedition.lcdui.Image;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.api.FormMultimediaController;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.utilities.media.MediaUtils;
import org.javarosa.utilities.media.VideoItem;

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
	private int scrHeight,scrWidth;
	
	protected VideoItem vItem;
	
	/** Used during image scaling **/
	public static int fallback = 99;
	
	/** The widget will provide it's own UI element for navigating to the next item **/
	public static final int NEXT_ON_MANUAL = 1;
	
	/** The widget will assume that as soon as a value is set, that the interface will navigate**/
	public static final int NEXT_ON_ENTRY = 2;
	
	/** The widget contains an item which can signal navigation, but doesn't have a manual element **/
	public static final int NEXT_ON_SELECT = 3;
	
	protected FormMultimediaController multimediaController;
	
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
		scrHeight = J2MEDisplay.getScreenHeight(ExpandedWidget.fallback);
		scrWidth = J2MEDisplay.getScreenWidth(ExpandedWidget.fallback);
		
		
		this.c = c;
	}
	public static ImageItem getImageItem(FormEntryPrompt fep,int height,int width) {
		String IaltText;

		IaltText = fep.getShortText();
		Image im = MediaUtils.getImage(fep.getImageText());
		if(im!=null){
			
			//scale
			int[] newDimension = MediaUtils.getNewDimensions(im, height, width);
			
			if(newDimension[0] != height || newDimension[1] != width) {
				im = MediaUtils.resizeImage(im, newDimension[1], newDimension[0]);
			}
			
			ImageItem imItem = new ImageItem(null,im, ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER, IaltText);
			imItem.setLayout(Item.LAYOUT_CENTER);
			return imItem;
		}else{
			return null;
		}
		
	}
	
	public static VideoItem getVideoItem(FormEntryPrompt fep) {
		try {
			String videoRef = fep.getSpecialFormQuestionText(FormEntryPrompt.TEXT_FORM_VIDEO);
			if(videoRef == null) { return null;}
			return new VideoItem(videoRef);
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	


	static int count = 0;
	private int imageIndex=-1;
	
	private ImageItem imItem;
	
	public void refreshWidget (FormEntryPrompt fep, int changeFlags) {
		
		//Look for and attach an image if one exists
		ImageItem newImItem = ExpandedWidget.getImageItem(fep,scrHeight/2,scrWidth-16); //width and height have been disabled!
		if(newImItem!=null){
			detachImage();
			fullPrompt.add(newImItem);
			imItem = newImItem;
		}
		
		//Look for and attach an image if one exists
		VideoItem newVItem = ExpandedWidget.getVideoItem(fep);
		if(newVItem!=null){
			detachVideo();
			getMultimediaController().attachVideoPlayer(newVItem.getPlayer());
			fullPrompt.add(newVItem);
			vItem = newVItem;
		}
		
		getMultimediaController().playAudioOnLoad(fep);
			
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
		detachImage();
		detachVideo();
		prompt = null;
		entryWidget = null;
	}

	public boolean focus () {
		//do nothing special
		return false;
	}
	
	public int getNextMode () {
		return ExpandedWidget.NEXT_ON_MANUAL;
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
	
	private void detachImage() {
		if(imItem!=null){
			fullPrompt.remove(imItem);	//replace an already existing image
			imItem.releaseResources();
			imItem = null;
		}
	}
	
	private void detachVideo() {
		if(vItem != null) {
			fullPrompt.remove(vItem);
			multimediaController.detachVideoPlayer(vItem.getPlayer());
			vItem.releaseResources();
			vItem = null;
		}
	}
	
	public void releaseMedia() {
		detachVideo();
	}

	public void registerMultimediaController(FormMultimediaController controller) {
		this.multimediaController = controller;
	}
	
	protected FormMultimediaController getMultimediaController() {
		if(this.multimediaController == null) {
			//If one isn't really set, use an empty shell
			return new FormMultimediaController() {

				public void playAudioOnLoad(FormEntryPrompt fep) {
					// TODO Auto-generated method stub
					
				}

				public void playAudioOnDemand(FormEntryPrompt fep) {
					// TODO Auto-generated method stub
					
				}

				public int playAudioOnDemand(FormEntryPrompt fep, SelectChoice select) {
					// TODO Auto-generated method stub
					return 0;
				}

				public void attachVideoPlayer(Player player) {
					// TODO Auto-generated method stub
					
				}

				public void detachVideoPlayer(Player player) {
					// TODO Auto-generated method stub
					
				}
			};

		} else {
			return multimediaController;
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
