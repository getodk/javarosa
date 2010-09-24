package org.javarosa.demo.util;

public class SavedFormListItem {

	private int formId;
	private int instanceId;
	private String formName;
	
	public SavedFormListItem(String formName,int formId,int instanceId)
	{
		this.setFormName(formName);
		this.setFormId(formId);
		this.setInstanceId(instanceId);
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public int getFormId() {
		return formId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getFormName() {
		return formName;
	}

	public String toString() {
		return this.getFormName()+"("+ this.getInstanceId() + ")" ;
	}
	
}
