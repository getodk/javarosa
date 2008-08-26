package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Date;

import javax.microedition.lcdui.DateField;

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

	public void creatView() {
		datePicker = new DateField(qDef.getShortText(), DateField.DATE);
		//set question
		datePicker.setLabel(qDef.getLongText());
		//check if the field has already been filled in by default value- if so display value
		if (qDef.getDefaultValue() != null){
			datePicker.setDate((Date)qDef.getDefaultValue());
		}
		this.append(datePicker);
		this.addNavigationButtons();
	}

	public IAnswerData getWidgetValue() {
		if(datePicker.getDate() != null){
		return new DateData(datePicker.getDate());}
		else{return null;}
	}


	public void setHint(String helpText) {
		// TODO Auto-generated method stub

	}

}