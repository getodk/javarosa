package org.javarosa.formmanager.view.chatterbox.widget;

import java.util.*;
import org.javarosa.formmanager.model.temp.*;
import de.enough.polish.ui.*;

public class DateEntryWidget extends ExpandedWidget {
	protected Item getEntryWidget (Prompt question) {
		//#style textBox
		return new DateField(null, DateField.DATE);
	}

	private DateField dateField () {
		return (DateField)entryWidget;    
	}

	protected void updateWidget (Prompt question) { /* do nothing */ }
	
	protected void setWidgetValue (Object o) {
		dateField().setDate((Date)o);
	}

	protected QuestionData getWidgetValue () {
		return new DateData(dateField().getDate());
	}
}