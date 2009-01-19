package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Command;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultiPointerAnswerData;
import org.javarosa.core.model.data.PointerAnswerData;

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
	
	protected Item getEntryWidget(QuestionDef question)
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
	protected void updateWidget(QuestionDef question) 
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
