package org.javarosa.formmanager.view.chatterbox.widget;

import de.enough.polish.ui.ChoiceGroup;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;

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
	}

	protected IAnswerData getWidgetValue () {
		int i = choiceGroup().getSelectedIndex();
		return (i == -1 ? null : new SelectOneData(new Selection(i, question)));
	}
	
	public boolean focus () {
		choiceGroup().focus(choiceGroup().getSelectedIndex());
		return true;
	}
}