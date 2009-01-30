package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Date;
import java.util.Calendar;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

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
		return ChatterboxWidget.NEXT_ON_SELECT;
	}
	
	protected IAnswerData getWidgetValue() 
	{
		Date d = dateField.getValue();
		return (d == null ? null : new DateData(d));
	}

	protected void updateWidget(QuestionDef question) 
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
	
	protected Item getEntryWidget(QuestionDef question) 
	{
		// TODO Auto-generated method stub
		return dateField;
	}
}
