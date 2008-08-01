package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;

import de.enough.polish.ui.ChoiceGroup;

public class SelectOneEntryWidget extends SelectEntryWidget {
	public SelectOneEntryWidget () {
		this(ChoiceGroup.EXCLUSIVE);
	}
	
	public SelectOneEntryWidget (int style) {
		super(style);
		
		if (style == ChoiceGroup.MULTIPLE)
			throw new IllegalArgumentException("Cannot use style 'MULTIPLE' on select1 control");
	}
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_ENTRY;
	}
	
	protected void setWidgetValue (Object o) {
		Selection s = (Selection)o;
		choiceGroup().setSelectedIndex(s.index, true);
	}

	protected IAnswerData getWidgetValue () {
		int selectedIndex = -1;
		
		for (int i = 0; i < choiceGroup().size(); i++) {
			if (choiceGroup().isSelected(i)) {
				selectedIndex = i;
				break;
			}
		}
		
		return (selectedIndex == -1 ? null : new SelectOneData(new Selection(selectedIndex, question)));
	}
	
	public boolean focus () {
		choiceGroup().focus(choiceGroup().getSelectedIndex());
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_SELECT_ONE;
	}
}