package org.javarosa.patient.entry.model;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.instance.QuestionDataElement;

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
