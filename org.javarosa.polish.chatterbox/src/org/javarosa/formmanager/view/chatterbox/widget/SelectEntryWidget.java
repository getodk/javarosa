package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Item;

import org.javarosa.clforms.api.Prompt;

public abstract class SelectEntryWidget extends ExpandedWidget {
	private int style;
	protected Prompt question;
	
	public SelectEntryWidget (int style) {
		this.style = style;
	}
	
	protected Item getEntryWidget (Prompt question) {
		this.question = question;
		ChoiceGroup cg = new ChoiceGroup("", style);
		
		for (int i = 0; i < question.getSelectMap().size(); i++)
			cg.append("", null);
		
		return cg;
	}

	protected ChoiceGroup choiceGroup () {
		return (ChoiceGroup)entryWidget;    
	}

	protected void updateWidget (Prompt question) {
		for (int i = 0; i < choiceGroup().size(); i++) {
			choiceGroup().getItem(i).setText((String)question.getSelectMap().keyAt(i));
		}
	}
}