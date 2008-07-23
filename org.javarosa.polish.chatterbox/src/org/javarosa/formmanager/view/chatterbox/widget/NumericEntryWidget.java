package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.QuestionDef;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

public class NumericEntryWidget extends TextEntryWidget {	
	protected Item getEntryWidget (QuestionDef question) {
		TextField tf = (TextField)super.getEntryWidget(question);
	    tf.setConstraints(TextField.NUMERIC);
		return tf;
	}

	//further overrides to deal with number data instead of strings?
}