package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class Select1QuestionWidget extends SingleQuestionScreen
{
	protected ChoiceGroup cg;

	public Select1QuestionWidget(FormElementBinding question) {
		super(question);
	}
	
	public Select1QuestionWidget(FormElementBinding prompt, int num) {
		super (prompt,num);
	}
	public Select1QuestionWidget(FormElementBinding prompt, String str) {
		super (prompt,str);
	}
	
	public Select1QuestionWidget(FormElementBinding prompt, char c) {
		super (prompt,c);
	}
	

	public void creatView() {
		if(qDef.instanceNode.required)
		{
			//#style choiceGroup
			cg = new ChoiceGroup("*"+((QuestionDef)qDef.element).getLongText(),ChoiceGroup.EXCLUSIVE );
		}
		else{
			//#style choiceGroup
			cg = new ChoiceGroup(((QuestionDef)qDef.element).getLongText(),ChoiceGroup.EXCLUSIVE );}

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

		int selectedIndex = -1;

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {
				selectedIndex = i;
				break;
			}
		}
		return (selectedIndex == -1 ? null : new SelectOneData(new Selection(selectedIndex, ((QuestionDef)qDef.element))));
	}


}