package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class SelectQuestionWidget extends SingleQuestionScreen
{
	ChoiceGroup cg;

	public SelectQuestionWidget(QuestionDef question){
		super(question);
	}

	public void creatView()
	{
		cg = new ChoiceGroup(qDef.getLongText(),ChoiceGroup.MULTIPLE ); //{
		Enumeration itr = qDef.getSelectItems().keys();//access choices directly
		int i = 0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			cg.append(label, null);//add options to choice group
			i++;
		}
		this.append(cg);
		this.addNavigationButtons();
	}

	public IAnswerData getWidgetValue() {
		// TODO Auto-generated method stub
		return null;
	}
	//Utility methods
	public void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}
}