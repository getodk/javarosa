package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Date;

import javax.microedition.lcdui.DateField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class TimeQuestionWidget extends SingleQuestionScreen
{
	protected DateField timePicker;

	public TimeQuestionWidget(QuestionDef question) {
		super(question);
	}

	public void creatView() {
		timePicker = new DateField(qDef.getShortText(), DateField.TIME);
		//set question
		timePicker.setLabel(qDef.getLongText());
		//check if the field has already been filled in by default value- if so display value
		if (qDef.getDefaultValue() != null){
			timePicker.setDate((Date)qDef.getDefaultValue());
		}
		this.append(timePicker);
		this.addNavigationButtons();
		if (qDef.getHelpText()!=null){
			setHint(qDef.getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {

		if(timePicker.getDate() != null){
			return new DateData(timePicker.getDate());}
			else{return null;}
	}

}