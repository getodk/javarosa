package org.javarosa.formmanager.view.chatterbox.util;

import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle;

public class ChatterboxContext extends FormEntryContext {
	public static final String CUSTOM_WIDGET_KEY = "CUSTOM_WIDGET_KEY";
	public ChatterboxContext(Context context) {
		super(context);
	}
	/**
	 * @param widget A new widget style to be added to the set of custom widgets
	 */
	public void addCustomWidget(IWidgetStyle widget) {
		Vector customWidgets = (Vector)getElement(CUSTOM_WIDGET_KEY);
		if(customWidgets == null) { 
			customWidgets = new Vector();
		}
		customWidgets.addElement(widget);
		setElement(CUSTOM_WIDGET_KEY,customWidgets);
	}
	/**
	 * @return A Vector of IWidgetStyles which are the custom widgets
	 */
	public Vector getCustomWidgets() {
		return (Vector)getElement(CUSTOM_WIDGET_KEY);
	}
}
