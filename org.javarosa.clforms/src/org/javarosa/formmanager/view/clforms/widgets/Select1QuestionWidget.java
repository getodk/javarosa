package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Ticker;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class Select1QuestionWidget extends SingleQuestionScreen
{
	protected ChoiceGroup cg;
	QuestionDef q;

	public Select1QuestionWidget(QuestionDef question) {
		super(question);
		this.q=question;
	}

	public void creatView() {
		cg = new ChoiceGroup(qDef.getLongText(),ChoiceGroup.EXCLUSIVE ); //{
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

		int selectedIndex = -1;

		for (int i = 0; i < cg.size(); i++) {
			if (cg.isSelected(i)) {
				selectedIndex = i;
				break;
			}
		}

		return (selectedIndex == -1 ? null : new SelectOneData(new Selection(selectedIndex, q)));
	}


}