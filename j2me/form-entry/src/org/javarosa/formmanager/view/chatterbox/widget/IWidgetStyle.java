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

package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Container;

/**
 * Strategy object that knows how to build and maintain a widget that contains a view of a single form question. This interface
 * only supports read-only views; there is no provision to get data from the user back out (see IWidgetStyleEditable).
 */
public interface IWidgetStyle {
	/**
	 * Initialize the structure of the widget, agnostic of the question's current locale and data value. This called is
	 * immediately followed by a call to refreshWidget(). This method should save off any references to GUI object that
	 * are necessary to update the widget later in refreshWidget(). If the high-level structure of the widget varies
	 * dynamically, the reference to 'c' itself should be saved.
	 * 
	 * @param question question object this widget represents
	 * @param c top-level container item of the widget, to which GUI items are added. 'c' may also be styled.
	 */
	void initWidget (FormEntryPrompt prompt, Container c);
	
	/**
	 * Refresh the widget in response to a change in the question, such as locale, data value, etc. Will be called
	 * immediately after initWidget().
	 * 
	 * @param bind grouper class containing information about the question
	 * @param changeFlags bitmap represent what specifically has changed in the question (see QuestionStateListener)
	 */
	void refreshWidget (FormEntryPrompt prompt, int changeFlags);
	
	/**
	 * Erase all state associated with this widget. If this widget is to be used again, initWidget() will be called again
	 * first.
	 */
	void reset ();
	
	/**
	 * @return A unique integer ID that will be used to represent this widget type
	 */
	int widgetType();
	
	/** 
	 * @return The height of this widget that, when taken off screen, should result
	 * in a pinned header.
	 */
	int getPinnableHeight();
}
