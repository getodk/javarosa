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

	public void creatView() {
		setHint("Type in your answer");
		//#style textBox
		 tf = new TextField("", "", 200, TextField.ANY);
		tf.setLabel(qDef.getLongText());
		this.append(tf);
		this.addNavigationButtons();
	}

	public IAnswerData getWidgetValue () {
		String s = tf.getString();
		return (s == null || s.equals("") ? null : new StringData(s));
	}

	public void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}
}