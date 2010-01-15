package org.javarosa.formmanager.view.chatterbox;

import java.util.Vector;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

public class FakedFormEntryPrompt extends FormEntryPrompt {
	
	private String text;
	private int controlType;
	private int dataType;
	
	private Vector<SelectChoice> choices;

	public FakedFormEntryPrompt(String text, int controlType, int dataType) {
		this.text = text;
		this.controlType = controlType;
		this.dataType = dataType;
		choices = new Vector<SelectChoice>();
	}

	public String getAnswerText() {
		return null;
	}

	public IAnswerData getAnswerValue() {
		// TODO Auto-generated method stub
		return null;
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

	public Vector<SelectChoice> getSelectChoices() {
		return choices;
	}
	
	public void addSelectChoice(SelectChoice choice) {
		choices.addElement(choice);
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
