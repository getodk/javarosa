package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

public class SelectMultiEntryWidget extends SelectEntryWidget {
	//#style button
	private StringItem nextItem = new StringItem(null,"Endelea",Item.BUTTON);

	
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
	
	protected Item getEntryWidget(QuestionDef question) {
		// TODO Auto-generated method stub
		ChoiceGroup cg = (ChoiceGroup)super.getEntryWidget(question);
		Item i = (Item)cg;
		
		//#if chatterbox.selectmulti.nextbutton
		Container c = new Container(false);
			
		c.add(cg);
		c.add(this.nextItem);
		i = (Item)c;
		//#endif
		
		return i;
	}
	
	public Item getInteractiveWidget() {
		Item i = super.getInteractiveWidget();
		//#if chatterbox.selectmulti.nextbutton
		i = this.nextItem;
		//#endif
		return i;
	}

	public int getNextMode() {
		int i = super.getNextMode();
		//#if chatterbox.selectmulti.nextbutton
		i = ChatterboxWidget.NEXT_ON_SELECT;
		//#endif
		return i;
	}

	protected IAnswerData getWidgetValue () {
		Vector vs = new Vector();
		
		for (int i = 0; i < choiceGroup().size(); i++) {
			if (choiceGroup().isSelected(i))
				vs.addElement(new Selection(i, question));
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