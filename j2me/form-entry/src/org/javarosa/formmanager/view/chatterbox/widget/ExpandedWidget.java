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
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.utility.WidgetUtil;

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
	protected boolean playAudioIfAvailable = true;

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
	
	/**
	 * Checks the boolean playAudioIfAvailable first.
	 */
	public void getAudioAndPlay(FormEntryPrompt fep){
		System.out.println(getCaptureSupport("audio"));
		System.out.println("-------------------");
		System.out.println(Manager.getSupportedContentTypes(null)[0]);
		System.out.println("--------------------");
		System.out.print("In getAudioAndPlay()...");
		if(!playAudioIfAvailable) return;
		System.out.print("---");
		Player player;
		String AudioURI;
		String textID = fep.getQuestion().getTextID();
		if(fep.getAvailableTextFormTypes(textID).contains("audio")){
			int index = fep.getAvailableTextFormTypes(textID).indexOf("audio");
			AudioURI = (String)fep.getAllTextForms(textID).elementAt(index);
			System.out.println("Question contains a legit audio URI:"+AudioURI+",attempting to get player and play...");
		}else{
			return;
		}
		try{
			Reference audRef = ReferenceManager._().DeriveReference(AudioURI);
			player = Manager.createPlayer(audRef.getStream(),"audio/x-wav");
//			player = new AudioPlayer("audio/mp3");
//			player.play(audRef.getStream());
			player.start();
			System.out.println("Should have played at this point.");
		}catch(InvalidReferenceException ire){
			throw new RuntimeException("Invalid Reference Exception when attempting to play audio at URI:"+AudioURI);
		}catch(IOException ioe){
			throw new RuntimeException("IO Exception (input cannot be read) when attempting to play audio stream with URI:"+AudioURI);
		} catch (MediaException e) {
			throw new RuntimeException("Media format not supported! Uri: "+AudioURI);
		}
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
				im = Image.createImage(ReferenceManager._().DeriveReference(Iuri).getStream());
			} catch (IOException e) {
				throw new RuntimeException("ERROR! Cant find image at URI:"+Iuri);	
			} catch (InvalidReferenceException ire){
				throw new RuntimeException("Invalid Reference for image at:"+Iuri+", with TextID ["+ ((sc != null) ? sc.getTextID() : fep.getTextID())+"]");
			}
		}
		
		return im;
	}

	public void refreshWidget (FormEntryPrompt fep, int changeFlags) {
		
		ImageItem imItem = getImageItem(fep);
		if(imItem!=null) c.add(imItem);
		
		prompt.setText(WidgetUtil.getAppropriateTextForm(fep,fep.getTextID()));
		getAudioAndPlay(fep);	
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
	
	private String getCaptureSupport(String media) {
		StringBuffer sbuf = new StringBuffer(media);
		String support = System.getProperty("supports." + media + ".capture");

		if ((support != null) && (!support.trim().equals(""))) {
			sbuf.append(" capture supported");
			String supportRecording = System.getProperty("supports.recording");
			if ((supportRecording != null)
					&& (!supportRecording.trim().equals(""))) {
				String recordingEncodings = System.getProperty(media
						+ ".encodings");
				if ((recordingEncodings != null)
						&& (!recordingEncodings.trim().equals(""))) {
					sbuf.append(", recording supported to ");
					sbuf.append(recordingEncodings);
				} else {
					sbuf
							.append(", recording not supported for this media format");
				}
			} else {
				sbuf.append(", recording not supported");
			}

			String snapshotEncodings = System.getProperty(media
					+ ".snapshot.encodings");

			if (snapshotEncodings != null) {
				sbuf.append(", snapshots supported to ");
				sbuf.append(snapshotEncodings);
			}
		} else {
			sbuf.append(" capture not supported");
		}
		return sbuf.toString();
	}
}
