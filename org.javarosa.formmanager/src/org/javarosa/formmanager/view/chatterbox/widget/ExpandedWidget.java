package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import org.javarosa.formmanager.model.temp.Prompt;
import org.javarosa.formmanager.model.temp.QuestionData;

import de.enough.polish.ui.Container;

public abstract class ExpandedWidget implements IWidgetStyleEditable {
	private StringItem prompt;
	protected Item entryWidget;

	public ExpandedWidget () {
		reset();
	}

	public void initWidget (Prompt question, Container c) {
		//#style questiontext
		prompt = new StringItem(null, null);
		entryWidget = getEntryWidget(question);
		//#style textBox
		entryWidget.setStyle(); //polish pre-processing turns this into a valid method call
		
		c.add(prompt);
		c.add(entryWidget);
	}

	public void refreshWidget (Prompt question, QuestionData data, int changeFlags) {
		prompt.setText(question.getLongText());
		updateWidget(question);
		setWidgetValue(data.getValue());
	}

	public QuestionData getData () {
		return getWidgetValue();
	}

	public void reset () {
		prompt = null;
		entryWidget = null;
	}

	protected abstract Item getEntryWidget (Prompt question);
	protected abstract void updateWidget (Prompt question);
	protected abstract void setWidgetValue (Object o);
	protected abstract QuestionData getWidgetValue ();
}
