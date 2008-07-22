package org.javarosa.formmanager.view.chatterbox.widget;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

import org.javarosa.core.model.QuestionDef;

public class NumericEntryWidget extends TextEntryWidget {	
	protected Item getEntryWidget (QuestionDef question) {
		TextField tf = (TextField)super.getEntryWidget(question);
	    tf.setConstraints(TextField.NUMERIC);
		return tf;
	}

	//further overrides to deal with number data instead of strings?
}