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

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.PointerAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

/**
 * This class represents a widget to handle audio capture 
 * for the Chatterbox interface
 * 
 * @author Ndubisi Onuora
 *
 */

public class AudioCaptureWidget extends ExpandedWidget 
{
	private PointerAnswerData data;	
	private StringItem label;
	
	protected Item getEntryWidget(FormEntryPrompt prompt)
	{
		updateLabel();
		return label;
	}
	
	//@Override
	protected IAnswerData getWidgetValue() 
	{
		return data;
	}

	//@Override
	protected void updateWidget(FormEntryPrompt prompt) 
	{
		// TODO Auto-generated method stub
	}

	protected void setWidgetValue(Object o)
	{
		IDataPointer objectPointer = (IDataPointer)o;
		if (objectPointer != null) 
		{
			data = new PointerAnswerData((IDataPointer) o);
		} 
		else 
		{
			data = null;
		}
		updateLabel();
	}
	
	//@Override
	public int widgetType() 
	{
		// TODO Auto-generated method stub
		return Constants.CONTROL_AUDIO_CAPTURE;
	}
	
	private void updateLabel()
	{
		if(label == null) 
		{
			label = new StringItem("", "");
			// this command comes from the other constants file
			Command recordCommand = new Command(org.javarosa.core.api.Constants.ACTIVITY_TYPE_GET_AUDIO, "Get Audio", Command.SCREEN, 0);
			label.addCommand(recordCommand);
		}
		if(data == null)
		{
			label.setLabel("Use the menu to capture audio");
		}
	}

}
