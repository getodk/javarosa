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

/**
 * 
 */
package org.javarosa.polish.activity.loadingscreen;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import org.javarosa.core.api.IView;

/**
 * @author Brian DeRenzi
 *
 */
public class LoadingView extends Form implements IView {
	
	//private static final Command cancelCmd = new Command("Cancel",Command.BACK, 2);
	
	private Gauge g = null;
	
	public LoadingView() {
		super("Loading");
		
		// Setup the screen
		this.g = new Gauge("Loading", false, Gauge.INDEFINITE,
				Gauge.INCREMENTAL_IDLE);
		append(this.g);
		
		
		// much too complicated right now...
		//addCommand(cancelCmd);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		// we're based on a form
		return this;
	}
	
	public void update() {
		this.g.setValue(Gauge.INCREMENTAL_UPDATING);
	}
}
