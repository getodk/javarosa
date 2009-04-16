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
		
		int preselectionIndex=-1; //index of the preset value for the question, if any
		String preselectionLabel=qDef.instanceNode.getValue()!=null?qDef.instanceNode.getValue().getDisplayText():null;
		int count = 0;
		
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			
			//check if the value is equal to the preset for this question
			if ((preselectionLabel!=null)&&(label.equals(preselectionLabel)))
				preselectionIndex=count;
			
			cg.append(label, null);//add options to choice group
			
			count++;
		}
		this.append(cg);
		
		//set the selection to the preset value, if any
		if ((preselectionIndex>-1)&&(preselectionIndex < cg.size()))
			cg.setSelectedIndex(preselectionIndex, true);
		
		
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
		
		QuestionDef question = (QuestionDef)qDef.element; 
		Selection s = new Selection((String)question.getSelectItemIDs().elementAt(selectedIndex));
		s.setQuestionDef(question);
		
		return (selectedIndex == -1 ? null : new SelectOneData(s));
	}


}