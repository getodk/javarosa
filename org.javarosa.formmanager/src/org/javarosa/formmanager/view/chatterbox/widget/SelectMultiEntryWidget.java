package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.core.model.QuestionData;
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

		//set focus?
	}

	protected QuestionData getWidgetValue () {
		Vector vs = new Vector();
		
		for (int i = 0; i < choiceGroup().size(); i++) {
			if (choiceGroup().isSelected(i))
				vs.addElement(new Selection(i, question));
		}		
		
		return new SelectMultiData(vs);
	}
}