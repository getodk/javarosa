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

import javax.microedition.lcdui.Image;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.CustomChoiceGroup;
import org.javarosa.utilities.media.MediaUtils;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Item;

/**
 * The base widget for multi and single choice selections.
 * 
 * NOTE: This class has a number of Hacks that I've made after rooting through
 * the j2me polish source. If polish is behaving unpredictably, it is possibly because
 * of conflicting changes in new Polish versions with this code. I have outlined
 * the latest versions of polish in which the changes appear to work.
 * 
 * Questions should be directed to csims@dimagi.com
 * 
 * @author Clayton Sims
 * @date Feb 16, 2009 
 *
 */
public abstract class SelectEntryWidget extends ExpandedWidget {
	private int style;
	private boolean autoSelect;
	protected FormEntryPrompt prompt;
	
	private CustomChoiceGroup choicegroup;
	
	public SelectEntryWidget (int style, boolean autoSelect) {
		this.style = style;
		this.autoSelect = autoSelect;
	}
	
	protected Item getEntryWidget (FormEntryPrompt prompt) {
		this.prompt = prompt;
		
		CustomChoiceGroup cg = new CustomChoiceGroup("",style, autoSelect) {
			public void playAudio(int index) {
				getMultimediaController().playAudioOnDemand(SelectEntryWidget.this.prompt,
															SelectEntryWidget.this.prompt.getSelectChoices().elementAt(index));
			}
		};
		for (int i = 0; i < prompt.getSelectChoices().size(); i++){
			cg.append("", null);
		}
		
		this.choicegroup = cg;
		
		return cg;
	}

	protected CustomChoiceGroup choiceGroup () {
		return this.choicegroup;
	}
	
	protected void updateWidget (FormEntryPrompt prompt) {
		for (int i = 0; i < choiceGroup().size(); i++) {
			SelectChoice sc = prompt.getSelectChoices().elementAt(i);
			Image im = MediaUtils.getImage(prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_IMAGE));

			choiceGroup().getItem(i).setText(prompt.getSelectChoiceText(sc));
			
			if(im!=null){
				choiceGroup().getItem(i).setImage(im);			
			}
		}
	}
}
