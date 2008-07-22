package org.javarosa.formmanager.view.chatterbox.widget;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Container;

public abstract class ExpandedWidget implements IWidgetStyleEditable {
	private StringItem prompt;
	protected Item entryWidget;

	public ExpandedWidget () {
		reset();
	}

	public void initWidget (QuestionDef question, Container c) {
		//#style questiontext
		prompt = new StringItem(null, null);
		entryWidget = getEntryWidget(question);
		//#style textBox
		entryWidget.setStyle(); //polish pre-processing turns this into a valid method call
		
		c.add(prompt);
		c.add(entryWidget);
	}

	public void refreshWidget (QuestionDef question, IAnswerData data, int changeFlags) {
		prompt.setText(question.getLongText());
		updateWidget(question);
		if (data != null) {
			setWidgetValue(data.getValue());
		}
	}

	public IAnswerData getData () {
		return getWidgetValue();
	}

	public void reset () {
		prompt = null;
		entryWidget = null;
	}

	public boolean focus () {
		//do nothing special
		return false;
	}
	
	protected abstract Item getEntryWidget (QuestionDef question);
	protected abstract void updateWidget (QuestionDef question);
	protected abstract void setWidgetValue (Object o);
	protected abstract IAnswerData getWidgetValue ();
}
