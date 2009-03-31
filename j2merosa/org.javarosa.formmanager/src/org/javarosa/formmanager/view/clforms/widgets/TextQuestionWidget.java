package org.javarosa.formmanager.view.clforms.widgets;

import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class TextQuestionWidget extends SingleQuestionScreen {

	protected TextField tf;

	public TextQuestionWidget(FormElementBinding question){
		super(question);
	}
	
	public TextQuestionWidget(FormElementBinding prompt, int num) {
		super (prompt,num);
	}
	public TextQuestionWidget(FormElementBinding prompt, String str) {
		super (prompt,str);
	}
	public TextQuestionWidget(FormElementBinding prompt, char c) {
		super (prompt,c);
	}

	public void creatView() {
		setHint("Type in your answer");
		//#style textBox
		 tf = new TextField("", "", 200, TextField.ANY);
		 if(qDef.instanceNode.required)
				tf.setLabel("*"+((QuestionDef)qDef.element).getLongText()); //visual symbol for required
				else
					tf.setLabel(((QuestionDef)qDef.element).getLongText());
		 
		 IAnswerData answerData = qDef.instanceNode.getValue();
		 if((answerData!=null)&& (answerData instanceof StringData))
			 tf.setString(((StringData)answerData).getDisplayText());
		 
		this.append(tf);
		this.addNavigationButtons();
		if (((QuestionDef)qDef.element).getHelpText()!=null){
			setHint(((QuestionDef)qDef.element).getHelpText());
		}
	}

	public IAnswerData getWidgetValue () {
		String s = tf.getString();
		return (s == null || s.equals("") ? null : new StringData(s));
	}

}