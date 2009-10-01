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

package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

public class NumericEntryWidget extends TextEntryWidget {
	private boolean isDecimal;
	
	public NumericEntryWidget() {
		this(false);
	}
	
	public NumericEntryWidget(boolean dec) {
		super();
		this.isDecimal = dec;
	}
	
	protected Item getEntryWidget (QuestionDef question) {
		TextField tf = textField();
		tf.setConstraints(isDecimal ? TextField.DECIMAL : TextField.NUMERIC);
		return super.getEntryWidget(question);
	}

	protected void setWidgetValue (Object o) {
		if(this.isDecimal)
			super.setWidgetValue(((Double)o).toString());
		else
			super.setWidgetValue(((Integer)o).toString());
	}
	
	protected IAnswerData getWidgetValue () {
		String s = textField().getText();
		
		if (s == null || s.equals(""))
			return null;
		
		double d = -999999999;
		int i = -99999;
		try {
			if(this.isDecimal)
				d = Double.parseDouble(s);
			else
				i = Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			System.err.println("Non-numeric data in numeric entry field!");
		}
		if(this.isDecimal)
			return new DecimalData(d);
		else
			return new IntegerData(i);
	}
}