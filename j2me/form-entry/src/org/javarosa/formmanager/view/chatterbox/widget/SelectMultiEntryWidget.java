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

import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Item;

public class SelectMultiEntryWidget extends SelectEntryWidget {
	private WidgetEscapeComponent wec = new WidgetEscapeComponent();
	
	public SelectMultiEntryWidget () {
		super(ChoiceGroup.MULTIPLE);
	}
	
	protected void setWidgetValue (Object o) {
		Vector vs = (Vector)o;
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
			if(s.index == -1) {
				s.attachQuestionDef(question);
			}
			if(s.index != -1) {
				choiceGroup().setSelectedIndex(s.index, true);
			} else {
				System.out.println("Invalid selection for multi select widget in value. Possibly due to changes in available values do to differing submit schema or a backup/restore. Selection value is " + s.xmlValue);
			}
		}
	}
	
	protected Item getEntryWidget(QuestionDef question) {
		return wec.wrapEntryWidget(super.getEntryWidget(question));
	}
	
	public Item getInteractiveWidget() {
		return wec.wrapInteractiveWidget(super.getInteractiveWidget());
	}

	public int getNextMode() {
		return wec.wrapNextMode(super.getNextMode());
	}

	protected IAnswerData getWidgetValue () {
		Vector vs = new Vector();
		
		for (int i = 0; i < choiceGroup().size(); i++) {
			if (choiceGroup().isSelected(i)) {
				Selection s = new Selection((String)question.getSelectItemIDs().elementAt(i));
				s.attachQuestionDef(question);
			
				vs.addElement(s);
			}
		}		
		
		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_SELECT_MULTI;
	}
}