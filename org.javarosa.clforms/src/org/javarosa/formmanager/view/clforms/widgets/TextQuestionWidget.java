package org.javarosa.formmanager.view.clforms.widgets;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.ItemCommandListener;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Form;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.StringItem;

public class TextQuestionWidget extends SingleQuestionScreen 
{
	
	protected QuestionDef prompt;
	protected Item entryWidget;
	protected TextField tf;
	private FormEntryController controller;
	SingleQuestionScreen parent;
	
	public TextQuestionWidget(String title)
	{	
		super(title);
		reset();
	}
	public Item initWidget(QuestionDef question)
	{
		System.out.println("Now performing text options");
		prompt = question;
		entryWidget = getEntryWidget(prompt);

		//set hint associated with
		setHint("Type in answer");
		
		return entryWidget;
	}
	
	protected Item getEntryWidget (QuestionDef question) 
	{
		//#style textBox
		 tf = new TextField("", "", 200, TextField.ANY);
		tf.setLabel(question.getLongText());
		return tf;
	}

	protected IAnswerData getWidgetValue () {
		String s = tf.getString();
		return (s == null || s.equals("") ? null : new StringData(s));
	}
	//Utility methods
	public void reset () 
	{
		prompt = null;
		entryWidget = null;
	}
	
	protected void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}

}