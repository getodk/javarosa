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

import javax.microedition.lcdui.Command;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

/**
 * Default read-only view of a question: a single frame with question QuestionDef (abbreviated) on the left/top and 
 * question answer in readable-text form on the right/bottom.
 */
public class CollapsedWidget implements IWidgetStyle {
	public static String UPDATE_TEXT = "Update";
	
	private static final Command updateCommand =  new Command(UPDATE_TEXT, Command.ITEM, 1);
	
	private StringItem prompt;
	private StringItem answer;
	
	private boolean seekable = false;

	public CollapsedWidget () {
		reset();
	}

	/**
	 * TODO: be smart about layout/wrapping; take into account lengths of QuestionDef and answer for optimum
	 * use of vertical screen space
	 */

	public void initWidget (FormEntryPrompt fep, Container c) {
		//#style split
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style splitleft
		Container test = new Container(false);
		prompt = new StringItem(null, null);
		if(this.seekable) {
			prompt.setDefaultCommand(updateCommand);
			test.setDefaultCommand(updateCommand);
		}
		test.add(new StringItem(null, null));
		test.add(prompt);
		
		//#style splitright
		answer = new StringItem(null, null); 

		//polish has a quirk where it really wants to impose the parent styling onto the first item in the
		//container, even if you explicitly override it with a new style. this null item takes the fall
		c.add(new StringItem(null, null));
		c.add(test);
		c.add(answer);
	}

	public void refreshWidget (FormEntryPrompt fep, int changeFlags) {		
		prompt.setText(fep.getShortText(null));
		
		IAnswerData data = fep.getAnswerValue();
		if (data != null) {
			answer.setText(data.getDisplayText());
		}
	}

	public void reset () {
		prompt = null;
		answer = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_UNTYPED;
	}
	
	public void setSeekable(boolean seekable) {
		this.seekable = seekable;
	}

	public int getPinnableHeight() {
		return prompt.getContentHeight();
	}
}
