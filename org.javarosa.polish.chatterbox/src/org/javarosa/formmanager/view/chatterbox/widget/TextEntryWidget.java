package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

public class TextEntryWidget extends ExpandedWidget {
	int inputMode;
	
	public TextEntryWidget () {
		this(TextField.MODE_UPPERCASE);
	}
	
	public TextEntryWidget (int inputMode) {
		this.inputMode = inputMode;
	}
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_SELECT;
	}
	
	protected Item getEntryWidget (QuestionDef question) {
		//#style textBox
		TextField tf = new TextField("", "", 200, TextField.ANY);
		tf.setInputMode(inputMode);
		return tf;
	}

	private TextField textField () {
		return (TextField)entryWidget;    
	}

	protected void updateWidget (QuestionDef question) { /* do nothing */ }
	
	protected void setWidgetValue (Object o) {
		textField().setText((String)o);
	}

	protected IAnswerData getWidgetValue () {
		String s = textField().getText();
		return (s == null ? null : new StringData(s));
	}
}