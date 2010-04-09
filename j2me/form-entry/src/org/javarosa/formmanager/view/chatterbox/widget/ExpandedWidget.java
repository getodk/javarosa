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

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public abstract class ExpandedWidget implements IWidgetStyleEditable {

	private StringItem prompt;
	protected Item entryWidget;

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
	}

	public void refreshWidget (FormEntryPrompt fep, int changeFlags) {
		String caption = fep.getLongText();
		
		////////####
		System.out.println("RICH MEDIA IMAGE uri="+fep.getImageURI());
		if(fep.getLongText()!=null && fep.getImageURI()!=null){
			caption+="  "+fep.getImageURI();
		}
		///////####
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
