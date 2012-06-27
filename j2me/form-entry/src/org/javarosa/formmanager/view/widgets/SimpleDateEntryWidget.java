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

import java.util.Date;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;

import de.enough.polish.ui.Item;

/**
 * 
 * @author Ndubisi Onuora
 *
 */

public class SimpleDateEntryWidget extends ExpandedWidget 
{
	private SimpleDateField dateField;
	
	public SimpleDateEntryWidget()
	{
		dateField = new SimpleDateField("");
	}
	
	public int getNextMode()
	{
		return ExpandedWidget.NEXT_ON_SELECT;
	}
	
	protected IAnswerData getWidgetValue() 
	{
		Date d = dateField.getValue();
		return (d == null ? null : new DateData(d));
	}

	protected void updateWidget(FormEntryPrompt prompt) 
	{
		// TODO Auto-generated method stub

	}
	
	protected void setWidgetValue(Object o)
	{
		dateField.setValue((Date)o);
	}

	public int widgetType() 
	{
		// TODO Auto-generated method stub
		return Constants.CONTROL_INPUT;
	}
	
	protected Item getEntryWidget(FormEntryPrompt prompt) 
	{
		// TODO Auto-generated method stub
		return dateField;
	}
	
	protected IAnswerData getAnswerTemplate() {
		return new DateData();
	}
}
