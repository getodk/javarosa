package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Date;

import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Item;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class TimeQuestionWidget extends SingleQuestionScreen
{
	protected Item entryWidget;
	protected QuestionDef qdef;

	public TimeQuestionWidget(String formTitle) {
		super(formTitle);
		reset();
	}
	
	public Item initWidget(QuestionDef question)
	{
		qdef = question;
		entryWidget = getEntryWidget(qdef);
		return entryWidget;
	}
	
	protected Item getEntryWidget(QuestionDef prompt)
	{
		DateField timePicker = new DateField(prompt.getShortText(), DateField.TIME);
		//set question 
		timePicker.setLabel(prompt.getLongText());
		//check if the field has already been filled in by default value- if so display value
		if (prompt.getDefaultValue() != null){
			timePicker.setDate((Date)prompt.getDefaultValue());
		}
		else
		{
			//need a way to retrieve the date entered for setDate()
		}
		return timePicker;
	}
	//Utility methods
	public void reset () 
	{
		qdef = null;
		entryWidget = null;
	}
}