package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.Selection;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class SelectQuestionWidget extends SingleQuestionScreen
{
	ChoiceGroup cg;
	QuestionDef q;

	public SelectQuestionWidget(QuestionDef question){
		super(question);
		this.q = question;
	}
	

	public SelectQuestionWidget(QuestionDef prompt, int num) {
		super (prompt,num);
		this.q = prompt;
	}
	public SelectQuestionWidget(QuestionDef prompt, String str) {
		super (prompt,str);
		this.q = prompt;
	}
	
	public SelectQuestionWidget(QuestionDef prompt, char c) {
		super (prompt,c);
		this.q = prompt;
	}

	public void creatView()
	{
		if(qDef.isRequired())
		{
			//#style choiceGroup
			cg = new ChoiceGroup("*"+qDef.getLongText(),ChoiceGroup.MULTIPLE );
		}
		else{
			//#style choiceGroup
			cg = new ChoiceGroup(qDef.getLongText(),ChoiceGroup.MULTIPLE );}
		Enumeration itr = qDef.getSelectItems().keys();//access choices directly
		int i = 0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			cg.append(label, null);//add options to choice group
			i++;
		}
		this.append(cg);
		this.addNavigationButtons();
		if (qDef.getHelpText()!=null){
			setHint(qDef.getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		Vector vs = new Vector();

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i))
				vs.addElement(new Selection(i, q));
		}

		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
}