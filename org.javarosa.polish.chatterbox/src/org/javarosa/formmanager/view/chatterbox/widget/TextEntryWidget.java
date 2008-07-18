package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import org.javarosa.clforms.api.Prompt;
import org.javarosa.core.model.QuestionData;
import org.javarosa.core.model.data.StringData;

public class TextEntryWidget extends ExpandedWidget {
	int inputMode;
	
	public TextEntryWidget () {
		this(TextField.MODE_UPPERCASE);
	}
	
	public TextEntryWidget (int inputMode) {
		this.inputMode = inputMode;
	}
	
	protected Item getEntryWidget (Prompt question) {
		//#style textBox
		TextField tf = new TextField("", "", 200, TextField.ANY);
		tf.setInputMode(inputMode);
		return tf;
	}

	private TextField textField () {
		return (TextField)entryWidget;    
	}

	protected void updateWidget (Prompt question) { /* do nothing */ }
	
	protected void setWidgetValue (Object o) {
		textField().setText((String)o);
	}

	protected QuestionData getWidgetValue () {
		return new StringData(textField().getText());
	}
}