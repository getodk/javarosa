package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;

import javax.microedition.lcdui.Command;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ChoiceGroup;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

public class SelectQuestionWidget extends SingleQuestionScreen
{
	private QuestionDef prompt;
	protected Item entryWidget;
	protected QuestionDef question;
	private int choiceType;
	
	public SelectQuestionWidget(String title)
	{
		super(title);
		reset();//clear widget on initialising
	}
	
	public Item initWidget(QuestionDef question)
	{
		//set question text
		prompt = question;
		getEntryWidget(prompt);		
		entryWidget = getEntryWidget(question);
		
		//set widget data value through IAnswerData
		//setWidgetValue(IAnswerData.getValue);
		
		//set hint
		setHint("Allowed to make multiple choices");
		return entryWidget;
	}
	
	public Item getEntryWidget (QuestionDef question) 
	{
		this.question = question;

		ChoiceGroup cg = new ChoiceGroup(question.getLongText(),ChoiceGroup.MULTIPLE ); //{
		Enumeration itr = question.getSelectItems().keys();//access choices directly
		int i = 0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			cg.append(label, null);//add options to choice group
			i++;
		}	
		
		return cg;
	}

	
	public void reset () {
		prompt = null;
		entryWidget = null;
	}
	
	//Utility methods
	protected void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}
	
}