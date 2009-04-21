package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class SelectQuestionWidget extends SingleQuestionScreen
{
	ChoiceGroup cg;

	public SelectQuestionWidget(FormElementBinding question){
		super(question);
	}
	

	public SelectQuestionWidget(FormElementBinding prompt, int num) {
		super (prompt,num);
	}
	public SelectQuestionWidget(FormElementBinding prompt, String str) {
		super (prompt,str);
	}
	
	public SelectQuestionWidget(FormElementBinding prompt, char c) {
		super (prompt,c);
	}

	public void creatView()
	{
		if(qDef.instanceNode.required)
		{
			//#style choiceGroup
			cg = new ChoiceGroup("*"+((QuestionDef)qDef.element).getLongText(),ChoiceGroup.MULTIPLE );
		}
		else{
			//#style choiceGroup
			cg = new ChoiceGroup(((QuestionDef)qDef.element).getLongText(),ChoiceGroup.MULTIPLE );}
		Enumeration itr = ((QuestionDef)qDef.element).getSelectItems().keys();//access choices directly
		int i = 0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			cg.append(label, null);//add options to choice group
			i++;
		}
		this.append(cg);
		this.addNavigationButtons();
		if (((QuestionDef)qDef.element).getHelpText()!=null){
			setHint(((QuestionDef)qDef.element).getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		Vector vs = new Vector();

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {
				QuestionDef q = (QuestionDef)qDef.element;
				Selection s  = new Selection((String)q.getSelectItemIDs().elementAt(i));
				s.attachQuestionDef(q);
				vs.addElement(s);
			}
		}

		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
}