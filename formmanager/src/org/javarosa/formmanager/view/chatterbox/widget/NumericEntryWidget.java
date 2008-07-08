package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.formmanager.model.temp.*;
import de.enough.polish.ui.*;

public class NumericEntryWidget extends TextEntryWidget {	
	protected Item getEntryWidget (Prompt question) {
		TextField tf = (TextField)super.getEntryWidget(question);
	    tf.setConstraints(TextField.NUMERIC);
		return tf;
	}

	//further overrides to deal with number data instead of strings?
}