/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
