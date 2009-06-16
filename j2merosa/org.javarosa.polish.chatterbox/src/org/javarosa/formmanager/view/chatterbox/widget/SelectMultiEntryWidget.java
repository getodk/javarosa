package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Item;

public class SelectMultiEntryWidget extends SelectEntryWidget {
	private WidgetEscapeComponent wec = new WidgetEscapeComponent();
	
	public SelectMultiEntryWidget () {
		super(ChoiceGroup.MULTIPLE);
	}
	
	protected void setWidgetValue (Object o) {
		Vector vs = (Vector)o;
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
			if(s.index == -1) {
				s.attachQuestionDef(question);
			}
			if(s.index != -1) {
				choiceGroup().setSelectedIndex(s.index, true);
			} else {
				System.out.println("Invalid selection for multi select widget in value. Possibly due to changes in available values do to differing submit schema or a backup/restore. Selection value is " + s.xmlValue);
			}
		}
	}
	
	protected Item getEntryWidget(QuestionDef question) {
		return wec.wrapEntryWidget(super.getEntryWidget(question));
	}
	
	public Item getInteractiveWidget() {
		return wec.wrapInteractiveWidget(super.getInteractiveWidget());
	}

	public int getNextMode() {
		return wec.wrapNextMode(super.getNextMode());
	}

	protected IAnswerData getWidgetValue () {
		Vector vs = new Vector();
		
		for (int i = 0; i < choiceGroup().size(); i++) {
			if (choiceGroup().isSelected(i)) {
				Selection s = new Selection((String)question.getSelectItemIDs().elementAt(i));
				s.attachQuestionDef(question);
			
				vs.addElement(s);
			}
		}		
		
		return (vs.size() == 0 ? null : new SelectMultiData(vs));
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_SELECT_MULTI;
	}
}