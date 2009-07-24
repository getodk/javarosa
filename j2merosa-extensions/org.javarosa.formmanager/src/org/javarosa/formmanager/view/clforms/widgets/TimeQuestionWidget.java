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
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class TimeQuestionWidget extends SingleQuestionScreen
{
	protected DateField timePicker;

	public TimeQuestionWidget(FormElementBinding question) {
		super(question);
	}
	
	public TimeQuestionWidget(FormElementBinding prompt, int num) {
		super (prompt,num);
	}
	public TimeQuestionWidget(FormElementBinding prompt, String str) {
		super (prompt,str);
	}
	public TimeQuestionWidget(FormElementBinding prompt, char c) {
		super (prompt,c);
	}
	
	public void creatView() {
		//#style textBox
		timePicker = new DateField(((QuestionDef)qDef.element).getShortText(), DateField.TIME);
		//set question
		 if(qDef.instanceNode.required)
				timePicker.setLabel("*"+((QuestionDef)qDef.element).getLongText()); //visual symbol for required
				else
					timePicker.setLabel(((QuestionDef)qDef.element).getLongText());

		// check if the field has already been filled in by default value- if so
		// display value
		IAnswerData answer = qDef.instanceNode.getValue();
		if ((answer != null) && (answer instanceof TimeData)) {
			timePicker.setDate((Date) (((TimeData) answer).getValue()));
		}

		this.append(timePicker);
		this.addNavigationButtons();
		if (((QuestionDef) qDef.element).getHelpText() != null) {
			setHint(((QuestionDef) qDef.element).getHelpText());
		}
	}

	public IAnswerData getWidgetValue() {

		if(timePicker.getDate() != null){
			return new TimeData(timePicker.getDate());}
			else{return null;}
	}

}