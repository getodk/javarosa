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

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IView;
import org.javarosa.core.util.IncompatibleViewException;

public class J2MEDisplay implements IDisplay {

	private final Display display;
	
	public J2MEDisplay(Display display) {
		this.display = display;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IDisplay#setView(org.javarosa.core.api.IView)
	 */
	public void setView(IView view) {
		if(view instanceof Displayable) {
			display.setCurrent((Displayable)view);
		} else if(view.getScreenObject() instanceof Displayable) {
			display.setCurrent((Displayable)view.getScreenObject());
		}
		else {
			throw new IncompatibleViewException("J2ME Display did not recognize the type of a view " + view.toString());
		}
	}
	
	public Object getDisplayObject() {
		return getDisplay();
	}
	
	public Display getDisplay() {
		return display;
	}
}
