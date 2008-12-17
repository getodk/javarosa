package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public abstract class ExpandedWidget implements IWidgetStyleEditable {
	private StringItem prompt;
	protected Item entryWidget;

	public ExpandedWidget () {
		reset();
	}

	public void initWidget (IFormElement element, Container c) {
		if(!(element instanceof QuestionDef)) {
			throw new IllegalArgumentException("element passed to refreshWidget that is not a QuestionDef");
		}
		QuestionDef question = (QuestionDef)element;
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		prompt = new StringItem(null, null);
		entryWidget = getEntryWidget(question);
		//#style textBox
		UiAccess.setStyle(entryWidget);
		
		c.add(prompt);
		c.add(entryWidget);
	}

	public void refreshWidget (IFormElement element, IAnswerData data, int changeFlags) {
		if(!(element instanceof QuestionDef)) {
			throw new IllegalArgumentException("element passed to refreshWidget that is not a QuestionDef");
		}
		QuestionDef question = (QuestionDef)element;
		prompt.setText(question.getLongText());
		updateWidget(question);
		
		//don't wipe out user-entered data, even on data-changed event
		if (data != null && changeFlags == FormElementStateListener.CHANGE_INIT) {
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
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_MANUAL;
	}
	
	public Item getInteractiveWidget () {
		return entryWidget;
	}
	
	protected abstract Item getEntryWidget (QuestionDef question);
	protected abstract void updateWidget (QuestionDef question);
	protected abstract void setWidgetValue (Object o);
	protected abstract IAnswerData getWidgetValue ();
}
