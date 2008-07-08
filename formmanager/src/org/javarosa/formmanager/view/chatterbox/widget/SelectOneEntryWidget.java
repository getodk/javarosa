package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.formmanager.model.temp.*;
import de.enough.polish.ui.*;

public class SelectOneEntryWidget extends SelectEntryWidget {
	public SelectOneEntryWidget () {
		this(ChoiceGroup.EXCLUSIVE);
	}
	
	public SelectOneEntryWidget (int style) {
		super(style);
		
		if (style == ChoiceGroup.MULTIPLE)
			throw new IllegalArgumentException("Cannot use style 'MULTIPLE' on select1 control");
	}
	
	protected void setWidgetValue (Object o) {
		Selection s = (Selection)o;
		choiceGroup().setSelectedIndex(s.index, true);
		//set focus?
	}

	protected QuestionData getWidgetValue () {
		return new SelectOneData(new Selection(choiceGroup().getSelectedIndex(), question));
	}
}