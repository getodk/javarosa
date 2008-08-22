package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Date;

import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Item;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class DateQuestionWidget extends SingleQuestionScreen
{
	protected Item entryWidget;
	protected QuestionDef qdef;
	protected DateField datePicker;
	
	public DateQuestionWidget(String formTitle) {
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
		datePicker = new DateField(prompt.getShortText(), DateField.DATE);
		//set question 
		datePicker.setLabel(prompt.getLongText());
		//check if the field has already been filled in by default value- if so display value
		if (prompt.getDefaultValue() != null){
			datePicker.setDate((Date)prompt.getDefaultValue());
		}
		else
		{
			//need a way to retrieve the date entered for setDate()
		}
		return datePicker;
	}
	//Utility methods
	public void reset () 
	{
		qdef = null;
		entryWidget = null;
	}


	public IAnswerData getWidgetValue() {
		
		return new DateData(datePicker.getDate());
	}


	public void setHint(String helpText) {
		// TODO Auto-generated method stub
		
	}
	
}