package org.javarosa.formmanager.utility;

import org.javarosa.core.Context;

public class TransportContext extends Context{
	
	public final static String MAIN_MENU = "main";
	public final static String MESSAGE_VIEW = "messages";
	
	public TransportContext(Context context) {
		super(context);
	}
	
	public String getRequestedView() {
		if(contextObject.containsKey("form-transport-view")) {
			return (String)contextObject.get("form-transport-view");
		}
		else {
			return MAIN_MENU;
		}
	}
	
	public void setRequestedView(String view) {
		contextObject.put("form-transport-view", view);
	}

}
