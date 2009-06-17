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
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class DecimalQuestionWidget extends SingleQuestionScreen
{
	protected TextField tf;
	//private boolean isDecimal=false;

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
		
		 IAnswerData answerData = qDef.instanceNode.getValue();
		 if((answerData!=null)&& (answerData instanceof DecimalData))
			 tf.setString(((DecimalData)answerData).getDisplayText());
		 
		 
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