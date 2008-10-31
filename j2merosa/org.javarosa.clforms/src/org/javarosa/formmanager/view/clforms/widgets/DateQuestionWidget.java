package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Date;

import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Ticker;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class DateQuestionWidget extends SingleQuestionScreen
{
	protected DateField datePicker;

	public DateQuestionWidget(QuestionDef qDef) {
		super(qDef);
	}
	
	public DateQuestionWidget(QuestionDef prompt, int num) {
		super (prompt,num);
	}
	public DateQuestionWidget(QuestionDef prompt, String str) {
		super (prompt,str);
	}
	public DateQuestionWidget(QuestionDef prompt, char c) {
		super (prompt,c);
	}

	public void creatView() {
		//#style textBox
		datePicker = new DateField(qDef.getShortText(), DateField.DATE);
		//set question
		if(qDef.isRequired())
		datePicker.setLabel("*"+qDef.getLongText());
		else
			datePicker.setLabel(qDef.getLongText());
		//check if the field has already been filled in by default value- if so display value
		if (qDef.getDefaultValue() != null){
			datePicker.setDate((Date)qDef.getDefaultValue());
		}
		this.append(datePicker);
		this.addNavigationButtons();
		if (qDef.getHelpText()!=null){
			setHint(qDef.getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		if(datePicker.getDate() != null){
		return new DateData(datePicker.getDate());}
		else{return null;}
	}

}