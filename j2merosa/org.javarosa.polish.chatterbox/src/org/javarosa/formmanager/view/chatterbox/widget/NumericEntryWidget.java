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