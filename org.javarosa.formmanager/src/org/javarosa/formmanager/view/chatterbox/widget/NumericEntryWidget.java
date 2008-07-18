package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import org.javarosa.clforms.api.Prompt;

public class NumericEntryWidget extends TextEntryWidget {	
	protected Item getEntryWidget (Prompt question) {
		TextField tf = (TextField)super.getEntryWidget(question);
	    tf.setConstraints(TextField.NUMERIC);
		return tf;
	}

	//further overrides to deal with number data instead of strings?
}