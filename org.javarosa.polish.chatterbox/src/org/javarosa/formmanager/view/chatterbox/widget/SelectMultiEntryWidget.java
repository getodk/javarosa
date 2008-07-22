package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Vector;

import de.enough.polish.ui.ChoiceGroup;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.Selection;

public class SelectMultiEntryWidget extends SelectEntryWidget {
	public SelectMultiEntryWidget () {
		super(ChoiceGroup.MULTIPLE);
	}
	
	protected void setWidgetValue (Object o) {
		Vector vs = (Vector)o;
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
			choiceGroup().setSelectedIndex(s.index, true);			
		}
	}

	protected IAnswerData getWidgetValue () {
		Vector vs = new Vector();
		
		for (int i = 0; i < choiceGroup().size(); i++) {
			if (choiceGroup().isSelected(i))
				vs.addElement(new Selection(i, question));
		}		
		
		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
}