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
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;

import de.enough.polish.ui.ChoiceGroup;

public class SelectOneEntryWidget extends SelectEntryWidget {
	public SelectOneEntryWidget () {
		this(ChoiceGroup.EXCLUSIVE);
	}
	
	public SelectOneEntryWidget (int style) {
		this(style, true, false);
	}

	public SelectOneEntryWidget(int style, boolean autoSelect, boolean numericNavigation) {
		super(style, autoSelect, numericNavigation);
		
		if (style == ChoiceGroup.MULTIPLE) {
			throw new IllegalArgumentException("Cannot use style 'MULTIPLE' on select1 control");
		}
	}
	
	public int getNextMode () {
		return ExpandedWidget.NEXT_ON_ENTRY;
	}
	
	protected void setWidgetValue (Object o) {
		Selection s = (Selection)o;
		if(s.index == -1) {
			s.attachChoice(prompt.getQuestion());
		}
		//To prevent audio from being played over if appropriate
		choiceGroup().setLastSelected(s.index);
		choiceGroup().setSelectedIndex(s.index, true);
		choiceGroup().touch();
	}

	protected IAnswerData getWidgetValue () {
		int selectedIndex = -1;
		
		for (int i = 0; i < choiceGroup().size(); i++) {
			if (choiceGroup().isSelected(i)) {
				selectedIndex = i;
				break;
			}
		}
		if(selectedIndex == -1) {
			return null;
		}
		
		Selection s = prompt.getSelectChoices().elementAt(selectedIndex).selection();
		return new SelectOneData(s);
	}
	
	public boolean focus () {
		choiceGroup().focusChild(choiceGroup().getSelectedIndex());
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_SELECT_ONE;
	}
	
	protected IAnswerData getAnswerTemplate() {
		return new SelectOneData();
	}
}