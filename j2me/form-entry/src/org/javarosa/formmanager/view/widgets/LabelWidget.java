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

import org.javarosa.core.model.Constants;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public class LabelWidget implements IWidgetStyle {
	private StringItem label;
	
	public LabelWidget() {
		super();
	}
	
	public void initWidget(FormEntryPrompt prompt, Container c) {
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		label = new StringItem(null, null);
		
		c.add(label);
	}

	public void refreshWidget(FormEntryPrompt prompt, int changeFlags) {
		// Clayton Sims - Feb 6, 2009 : Labels are now applicable for questions, so that we can pin 
		// question texts for long questions.
		String caption = prompt.getLongText();
		
		label.setText(caption);
	}

	public void reset() {
		label = null;
	}

	public int widgetType() {
		return Constants.CONTROL_LABEL;
	}
	
	public String toString() {
		return label.getText();
	}
	
	public Object clone() {
		return new LabelWidget();
	}

	public int getPinnableHeight() {
		return label.getContentHeight();
	}
}
