package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Item;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class Select1QuestionWidget extends SingleQuestionScreen
{
	private QuestionDef prompt;
	protected Item entryWidget;
	protected QuestionDef question;
	protected ChoiceGroup cg;
	
	public Select1QuestionWidget(String formTitle) {
		super(formTitle);
		reset();
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
		setHint("You must select only one option");
		return entryWidget;
	}
	
	public Item getEntryWidget (QuestionDef question) 
	{
		this.question = question;

		cg = new ChoiceGroup(question.getLongText(),ChoiceGroup.EXCLUSIVE ); //{
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
	public void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}

	public IAnswerData getWidgetValue() {
		
		return null;
	}
	
}