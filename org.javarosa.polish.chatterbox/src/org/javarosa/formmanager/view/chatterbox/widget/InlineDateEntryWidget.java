package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.Date;
import java.util.Calendar;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;

/**
 * Widget class for InlineDateField.
 * By Thomas Smyth (tom@tomsmyth.ca)
 */
public class InlineDateEntryWidget extends ExpandedWidget {
	public final static int CONTROL_INLINE_DATE = 111; 

	InlineDateField dateField;
	
	public InlineDateEntryWidget() {
		dateField = new InlineDateField("");
	}
	
	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_SELECT;
	}
	
	protected Item getEntryWidget (QuestionDef question) {
		return dateField;
	}

	protected void updateWidget (QuestionDef question) { /* do nothing */ }
	
	protected void setWidgetValue (Object o) {
		dateField.setValue((Date)o);
	}

	protected IAnswerData getWidgetValue () {
		Date d = dateField.getValue();
		return (d == null ? null : new DateData(d));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_INPUT;
	}
}