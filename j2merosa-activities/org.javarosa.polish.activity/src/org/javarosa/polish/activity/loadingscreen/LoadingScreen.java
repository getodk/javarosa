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

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import org.javarosa.core.api.IView;
import org.javarosa.core.services.locale.Localization;

/**
 * @author Clayton Sims
 * @date Jan 21, 2009
 * 
 */
public class LoadingScreen extends Form implements IView {

	private Timer timer;

	public final static String LOADING_STR = Localization.get("view.loading");

	public LoadingScreen() {
		super(LOADING_STR);

		// Setup the screen
		final Gauge g = new Gauge(LOADING_STR, false, Gauge.INDEFINITE,
				Gauge.INCREMENTAL_IDLE);
		append(g);
		
		TimerTask r = new TimerTask() {
			public void run() {
				g.setValue(Gauge.INCREMENTAL_UPDATING);
			}

		};
		this.timer = new Timer();
		this.timer.schedule(r, 0, 500);
	}

	public void destroy() {
		this.timer.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
