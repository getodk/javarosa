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

package org.javarosa.j2me.view;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.IView;

public class DisplayViewFactory {
	
	/**
	 * Factory method to create a JavaRosa View from a 
	 * j2me displayable
	 * @param d The displayable to be wrapped
	 * @return An IView object that can be used by a j2me
	 * IDisplay
	 */
	public static IView createView(final Displayable d) {
		return new IView () {
			public Object getScreenObject() {
				return d;
			}
		};
	}
}
