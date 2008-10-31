package org.javarosa.patient.entry.model;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.instance.QuestionDataElement;

/**
 * NOTICE:
 * 
 * 
 * This class, and the entire org.javarosa.patient.entry Module all only
 * exist because <group> and <repeat> are not yet available in our XForms 
 * subset. 
 * 
 * THIS MODULE IS FULL OF CODE THAT SHOULD NOT BE EMULATED.
 * 
 * Please do not take the code in the org.javarosa.patient.entry project
 * as an example of a proper use of the JavaRosa code base. It will be removed
 * as soon as it is possible to represent the logic in the PatientEntryForm in
 * our XForms subset.
 * 
 * @author Clayton Sims
 *
 */
public class TriggerQuestionDataElement extends QuestionDataElement {

	ITriggerHandler triggerHandler;
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.instance.QuestionDataElement#setValue(org.javarosa.core.model.data.IAnswerData)
	 */
	public void setValue(IAnswerData value) {
		if(triggerHandler != null) {
			if(value != null) {
				if(value instanceof SelectOneData && ((SelectOneData)value).getDisplayText().equals("Yes")) {
					triggerHandler.handle();
				}
			}
		}
		super.setValue(value);
	}
	/**
	 * @return the triggerHandler
	 */
	public ITriggerHandler getTriggerHandler() {
		return triggerHandler;
	}
	/**
	 * @param triggerHandler the triggerHandler to set
	 */
	public void setTriggerHandler(ITriggerHandler triggerHandler) {
		this.triggerHandler = triggerHandler;
	}
}
