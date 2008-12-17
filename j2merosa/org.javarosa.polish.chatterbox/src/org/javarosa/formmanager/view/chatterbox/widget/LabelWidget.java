package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public class LabelWidget implements IWidgetStyle {
	private StringItem label;
	
	private int multiplicity = -1;
	
	public LabelWidget() {
		super();
	}
	
	public LabelWidget(int mult) {
		this();
		multiplicity = mult;
	}
	
	public void initWidget(IFormElement element, Container c) {
		//#style container
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now
		
		//#style questiontext
		label = new StringItem(null, null);
		
		c.add(label);
	}

	public void refreshWidget(IFormElement element, IAnswerData data,
			int changeFlags) {
		if(!(element instanceof GroupDef)) {
			throw new IllegalArgumentException("element passed to LabelWidget's refreshWidget that is not a GroupDef");
		}
		GroupDef group = (GroupDef)element;
		String text  = group.getLongText();
		if(multiplicity != -1) {
			text = text + ": " + multiplicity;
		}
		label.setText(text);

	}

	public void reset() {
		label = null;
	}

	public int widgetType() {
		return Constants.CONTROL_LABEL;
	}
	
	public String toString() {
		return label.getText();
	}
	
	public Object clone() {
		LabelWidget label = new LabelWidget(multiplicity);
		return label;
	}
}
