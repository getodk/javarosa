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

import java.util.Date;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.PointerAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.DateField;
import de.enough.polish.ui.Item;

public class DateEntryWidget extends ExpandedWidget {
	boolean dateTime = false;
	
	public DateEntryWidget() {
		this(false);
	}
	
	public DateEntryWidget(boolean dateTime) {
		super();
		this.dateTime = dateTime;
	}
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_ENTRY;
	}
	
	protected Item getEntryWidget (FormEntryPrompt prompt) {
		//#style textBox
		return new DateField(null, DateField.DATE);
	}

	private DateField dateField () {
		return (DateField)entryWidget;    
	}

	protected void updateWidget (FormEntryPrompt prompt) { /* do nothing */ }
	
	protected void setWidgetValue (Object o) {
		dateField().setDate((Date)o);
	}

	protected IAnswerData getWidgetValue () {
		Date d = dateField().getDate();
		if(d == null) {
			return null;
		}
		if (dateTime) {
			return  new DateTimeData(d);
		} else {
			return new DateData(d);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_INPUT;
	}
	
	protected IAnswerData getAnswerTemplate() {
		if (dateTime) {
			return new DateTimeData();
		} else {
			return new DateData();
		}
	}
}