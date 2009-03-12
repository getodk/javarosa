package org.javarosa.formmanager.view.clforms.widgets;

import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class DecimalQuestionWidget extends SingleQuestionScreen
{
	protected TextField tf;
	private boolean isDecimal=false;

	public DecimalQuestionWidget(FormElementBinding question){
		super(question);
	}
	
	public DecimalQuestionWidget(FormElementBinding prompt, int num) {
		super (prompt,num);
	}
	public DecimalQuestionWidget(FormElementBinding prompt, String str) {
		super (prompt,str);
	}
	public DecimalQuestionWidget(FormElementBinding prompt, char c) {
		super (prompt,c);
	}
	
	

	public void creatView() {
		
		//#style textBox
		 tf = new TextField("", "", 200, TextField.DECIMAL);	
		
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
		
		if (s == null || s.equals(""))
			return null;
		
		double d = -999999999;
		try {
			d = Double.parseDouble(s);
			
		} catch (NumberFormatException nfe) {
			System.err.println("Non-numeric data in numeric entry field!");
		}
		return new DecimalData(d);
		
	}

}