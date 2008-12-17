package org.javarosa.formmanager.view.clforms.widgets;

import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class NumericQuestionWidget extends SingleQuestionScreen
{
	protected TextField tf;

	public NumericQuestionWidget(FormElementBinding question){
		super(question);

	}
	
	public NumericQuestionWidget(FormElementBinding prompt, int num) {
		super (prompt,num);
	}
	public NumericQuestionWidget(FormElementBinding prompt, String str) {
		super (prompt,str);
	}
	public NumericQuestionWidget(FormElementBinding prompt, char c) {
		super (prompt,c);
	}
	

	public void creatView() {
		//#style textBox
		 tf = new TextField("", "", 200, TextField.NUMERIC);	
		 if(qDef.instanceNode.required)
				tf.setLabel("*"+((QuestionDef)qDef.element).getLongText()); //visual symbol for required
				else
					tf.setLabel(((QuestionDef)qDef.element).getLongText());
		this.append(tf);
		this.addNavigationButtons();
		if (((QuestionDef)qDef.element).getHelpText()!=null){
			setHint(((QuestionDef)qDef.element).getHelpText());
		}
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

}