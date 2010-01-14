package org.javarosa.formmanager.view.chatterbox;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.form.api.FormEntryPrompt;

public class FakedFormEntryPrompt extends FormEntryPrompt {
	
	private String text;
	private int controlType;
	private int dataType;

	public FakedFormEntryPrompt(String text, int controlType, int dataType) {
		this.text = text;
		this.controlType = controlType;
		this.dataType = dataType;
	}
	
	public String getAnswerText() {
		
	}

	public IAnswerData getAnswerValue() {
		// TODO Auto-generated method stub
		return super.getAnswerValue();
	}

	public String getConstraintText() {
		return null;
	}

	public int getControlType() {
		return controlType;
	}

	public int getDataType() {
		return dataType;
	}

	public String getHelpText() {
		return null;
	}

	public String getLongText() {
		return text;
	}

	public String getPromptAttributes() {
		return null;
	}

	public OrderedHashtable getSelectItems() {
		// TODO Auto-generated method stub
		return super.getSelectItems();
	}

	public String getShortText() {
		return text;
	}

	public boolean isReadOnly() {
		return false;
	}

	public boolean isRequired() {
		return true;
	}

}
