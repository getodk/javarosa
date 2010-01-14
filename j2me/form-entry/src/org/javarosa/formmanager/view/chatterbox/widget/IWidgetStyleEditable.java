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

import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Item;

/**
 * Strategy object that knows how to build and maintain a widget that contains a view of a single form question. This interface
 * supports returning data back from the widget.
 */
public interface IWidgetStyleEditable extends IWidgetStyle {
	/**
	 * Get the data currently entered in the widget, in the datatype appropriate to the type of question this
	 * widget is tied to. If this widget represents a question or concept that is not meant to collect input, return null.
	 * 
	 * @return currently entered question data; null if not applicable
	 */
	IAnswerData getData ();
	
	//handle custom widget focusing. return whether any focusing was performed (thus needing repaint)
	boolean focus ();
	
	//specify how the 'answered' event is triggered
	int getNextMode ();
	
	//get item to set event listeners for 
	Item getInteractiveWidget ();
}
