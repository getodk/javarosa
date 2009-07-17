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
		
		 IAnswerData answerData = qDef.instanceNode.getValue();
		 if((answerData!=null)&& (answerData instanceof IntegerData))
			 tf.setString(((IntegerData)answerData).getDisplayText()); 
		 
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