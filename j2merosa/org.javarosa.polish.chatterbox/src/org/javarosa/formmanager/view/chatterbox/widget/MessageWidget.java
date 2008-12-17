package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public class MessageWidget implements IWidgetStyleEditable {
	private StringItem prompt;
	private StringItem ok;

	public MessageWidget () {
		reset();
	}

	public void initWidget (IFormElement element, Container c) {
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		prompt = new StringItem(null, null);
		//#style button
		ok = new StringItem(null, "OK");
		
		c.add(prompt);
		c.add(ok);
	}

	public void refreshWidget (IFormElement element, IAnswerData data, int changeFlags) {
		if(!(element instanceof QuestionDef)) {
			throw new IllegalArgumentException("element passed to refreshWidget that is not a QuestionDef");
		}
		QuestionDef question = (QuestionDef)element;
		prompt.setText(question.getLongText());
	}

	public IAnswerData getData () {
		return null;
	}

	public void reset () {
		prompt = null;
		ok = null;
	}

	public boolean focus () {
		//do nothing special
		return false;
	}
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_SELECT;
	}
	
	public Item getInteractiveWidget () {
		return ok;
	}
	
	public int widgetType () {
		return Constants.CONTROL_TRIGGER;
	}
}
