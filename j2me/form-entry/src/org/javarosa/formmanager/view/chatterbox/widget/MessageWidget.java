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

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public class MessageWidget implements IWidgetStyleEditable {
	private StringItem prompt;
	private StringItem ok;

	public MessageWidget () {
		reset();
	}

	public void initWidget (FormEntryPrompt fep, Container c) {
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		prompt = new StringItem(null, null);
		//#style button
		ok = new StringItem(null, "OK");
		
		c.add(prompt);
		c.add(ok);
	}

	public void refreshWidget (FormEntryPrompt fep, int changeFlags) {
		prompt.setText(fep.getLongText());
	}

	public IAnswerData getData () {
		return null;
	}

	public void reset () {
		prompt = null;
		ok = null;
	}

	public boolean focus () {
		//do nothing special
		return false;
	}
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_SELECT;
	}
	
	public Item getInteractiveWidget () {
		return ok;
	}
	
	public int widgetType () {
		return Constants.CONTROL_TRIGGER;
	}

	public int getPinnableHeight() {
		return prompt.getContentHeight();
	}
}
