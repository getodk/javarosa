package org.javarosa.formmanager.view.clforms.widgets;

import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class TextQuestionWidget extends SingleQuestionScreen {

	protected TextField tf;

	public TextQuestionWidget(QuestionDef question){
		super(question);
	}
	
	public TextQuestionWidget(QuestionDef prompt, int num) {
		super (prompt,num);
	}
	public TextQuestionWidget(QuestionDef prompt, String str) {
		super (prompt,str);
	}
	public TextQuestionWidget(QuestionDef prompt, char c) {
		super (prompt,c);
	}

	public void creatView() {
		setHint("Type in your answer");
		//#style textBox
		 tf = new TextField("", "", 200, TextField.ANY);
		 if(qDef.isRequired())
				tf.setLabel("*"+qDef.getLongText()); //visual symbol for required
				else
					tf.setLabel(qDef.getLongText());
		this.append(tf);
		this.addNavigationButtons();
		if (qDef.getHelpText()!=null){
			setHint(qDef.getHelpText());
		}
	}

	public IAnswerData getWidgetValue () {
		String s = tf.getString();
		return (s == null || s.equals("") ? null : new StringData(s));
	}

}