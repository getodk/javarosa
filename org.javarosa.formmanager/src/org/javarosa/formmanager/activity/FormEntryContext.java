package org.javarosa.formmanager.activity;

import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.xpath.IFunctionHandler;

public class FormEntryContext extends Context {
	public static final String FORM_ID = "FORM_ID";
	public static final String INSTANCE_ID = "INSTANCE_ID";
	public static final String PRELOAD_HANDLERS = "PRELOAD_HANDLERS";
	public static final String FUNCTION_HANDLERS = "FUNCTION_HANDLERS";
	
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
		Integer i = (Integer)getElement(INSTANCE_ID);
		return (i == null ? -1 : i.intValue());
	}
	
	public void setInstanceID (int instanceID) {
		setElement(INSTANCE_ID, new Integer(instanceID));
	}
	
	public void addHandler (Object handler, String key) {
		Vector handlers = (Vector) getElement(key);
		if(handlers == null) {
			handlers = new Vector();
		}
		handlers.addElement(handler);
		setElement(key, handlers);	
	}
	
	public void addPreloadHandler(IPreloadHandler handler) { 
		addHandler(handler, PRELOAD_HANDLERS);
	}
	
	public Vector getPreloadHandlers() {
		return (Vector)getElement(PRELOAD_HANDLERS);
	}
	
	public void addFunctionHandler(IFunctionHandler handler) {
		addHandler(handler, FUNCTION_HANDLERS);
	}
	
	public Vector getFunctionHandlers() {
		return (Vector)getElement(FUNCTION_HANDLERS);
	}
}