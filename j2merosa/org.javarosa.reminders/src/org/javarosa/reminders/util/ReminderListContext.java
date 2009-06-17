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
