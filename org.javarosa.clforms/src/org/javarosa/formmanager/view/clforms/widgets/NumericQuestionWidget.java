package org.javarosa.formmanager.view.clforms.widgets;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class NumericQuestionWidget extends SingleQuestionScreen 
{
	
	protected QuestionDef prompt;
	protected Item entryWidget;
	protected TextField tf;
	SingleQuestionScreen parent;
	
	public NumericQuestionWidget(String title)
	{
		super(title);
	}
	
	public Item initWidget(QuestionDef question)
	{
		prompt = question;
		entryWidget = getEntryWidget(prompt);

		//set hint associated with
		setHint("Type in answer");
		
		return entryWidget;
	}
	
	protected Item getEntryWidget (QuestionDef question) 
	{
		//#style textBox
		 tf = new TextField("", "", 200, TextField.NUMERIC);
		tf.setLabel(question.getLongText());
		return tf;
	}

	public IAnswerData getWidgetValue () {
		String s = tf.getString();
		//if empty
		if (s == null || s.equals(""))
			return null;
		
		//check answer integrity
		int i = -99999;
		try {
			i = Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			System.err.println("Non-numeric data in numeric entry field!");
		}
		return new IntegerData(i);
	}
	//Utility methods
	public void reset () 
	{
		prompt = null;
		entryWidget = null;
	}
	
	public void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}

}