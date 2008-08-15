package org.javarosa.reminders.util;

import org.javarosa.core.Context;

public class ReminderListContext extends Context {
	public static final int VIEW_ALL = 1;
	public static final int VIEW_TRIGGERED = 2;
	
	public static final String VIEW_MODE_KEY = "Reminder View Mode";
	
	public ReminderListContext(Context context) {
		super(context);
	}
	
	public void setViewMode(int viewMode) {
		this.setElement(VIEW_MODE_KEY, new Integer(viewMode));
	}
	public int getViewMode() {
		Integer viewModeValue = (Integer)this.getElement(VIEW_MODE_KEY);
		int viewMode = (viewModeValue == null) ? VIEW_ALL : viewModeValue.intValue();
		return viewMode;
	}
}
