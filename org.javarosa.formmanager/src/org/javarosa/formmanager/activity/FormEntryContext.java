package org.javarosa.formmanager.activity;

import org.javarosa.core.*;

public class FormEntryContext extends Context {
	public static final String FORM_ID = "FORM_ID";
	public static final String INSTANCE_ID = "INSTANCE_ID";
	
	public FormEntryContext(Context context) { 
		super(context);
	}
	
	public int getFormID () {
		return ((Integer)getElement(FORM_ID)).intValue();
	}
	
	public void setFormID (int formID) {
		setElement(FORM_ID, new Integer(formID));
	}

	public int getInstanceID () {
		return ((Integer)getElement(INSTANCE_ID)).intValue();
	}
	
	public void setInstanceID (int instanceID) {
		setElement(INSTANCE_ID, new Integer(instanceID));
	}
}