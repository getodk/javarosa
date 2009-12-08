/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Date;

import javax.microedition.lcdui.DateField;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class DateQuestionWidget extends SingleQuestionScreen
{
	protected DateField datePicker;

	public DateQuestionWidget(FormElementBinding qDef) {
		super(qDef);
	}
	
	public DateQuestionWidget(FormElementBinding prompt, int num) {
		super (prompt,num);
	}
	public DateQuestionWidget(FormElementBinding prompt, String str) {
		super (prompt,str);
	}
	public DateQuestionWidget(FormElementBinding prompt, char c) {
		super (prompt,c);
	}

	public void creatView() {
		//#style textBox
		datePicker = new DateField(((QuestionDef)qDef.element).getShortText(), DateField.DATE);
		//set question
		if(qDef.instanceNode.required)
		datePicker.setLabel("*"+((QuestionDef)qDef.element).getLongText());
		else
			datePicker.setLabel(((QuestionDef)qDef.element).getLongText());
		
		// check if the field has already been filled in by default value- if so
		// display value
		IAnswerData answer = qDef.instanceNode.getValue();
		if ((answer != null) && (answer instanceof DateData)) {
			datePicker.setDate((Date) (((DateData) answer).getValue()));
		}
		this.append(datePicker);
		this.addNavigationButtons();
		if (((QuestionDef)qDef.element).getHelpText()!=null){
			setHint(((QuestionDef)qDef.element).getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {
		if(datePicker.getDate() != null){
		return new DateData(datePicker.getDate());}
		else{return null;}
	}

}