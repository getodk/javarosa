package org.javarosa.reminders.util;

import org.javarosa.core.model.DataBinding;
import org.javarosa.xform.util.IXFormBindHandler;
import org.kxml2.kdom.Element;

public class ReminderBindHandler implements IXFormBindHandler {

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.xform.util.IXFormBindHandler#handle(org.kxml2.kdom.Element, org.javarosa.core.model.DataBinding)
	 */
	public void handle(Element bindElement, DataBinding bind) {
		String reminder = bindElement.getAttributeValue("","reminder");
		if(reminder != null) {
			
		}
	}

}
