package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

public class NumericEntryWidget extends TextEntryWidget {	
	protected Item getEntryWidget (QuestionDef question) {
		TextField tf = (TextField)super.getEntryWidget(question);
	    tf.setConstraints(TextField.NUMERIC);
		return tf;
	}

	protected IAnswerData getWidgetValue () {
		String s = textField().getText();
		
		if (s == null || s.equals(""))
			return null;
		
		int i = -99999;
		try {
			i = Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			System.err.println("Non-numeric data in numeric entry field!");
		}
		return new IntegerData(i);
	}
}