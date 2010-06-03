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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.javarosa.core.api.State;
import org.javarosa.j2me.log.HandledThread;

public class J2MEDisplay {
	private static Display display;
	private static LoadingScreenThread loading;
	
	public static void init (MIDlet m) {
		setDisplay(Display.getDisplay(m));
		loading = new LoadingScreenThread(display);
	}
	
	public static void setDisplay (Display display) {
		J2MEDisplay.display = display;
	}

	public static Display getDisplay () {
		if (display == null) {
			throw new RuntimeException("Display has not been set from MIDlet; call J2MEDisplay.init()");
		}
		
		return display;
	}
	
	public static void startStateWithLoadingScreen(State state) {
		startStateWithLoadingScreen(state,null);
	}
	
	public static void startStateWithLoadingScreen(State state, ProgressIndicator indicator) {
		final State s = state;
		loading.cancelLoading();
		loading = new LoadingScreenThread(display);
		loading.startLoading(indicator);
		new HandledThread(new Runnable() {
			public void run() {
				s.start();
			}
		}).start();
	}
	
	public static void setView (Displayable d) {
		loading.cancelLoading();
		display.setCurrent(d);
	}
	
	public static void showError (String title, String message) {
		showError(title, message, null, null);
	}
	
	public static Alert showError (String title, String message, Displayable next, CommandListener customListener) {
		//#style mailAlert
		final Alert alert = new Alert(title, message, null, AlertType.ERROR);
		alert.setTimeout(Alert.FOREVER);

		if (customListener != null) {
			if (next != null) {
				System.err.println("Warning: alert invoked with both custom listener and 'next' displayable. 'next' will be ignored; it must be switched to explicitly in the custom handler");
			}
			
			alert.setCommandListener(customListener);
		}
		
		if (next == null) {
			setView(alert);
		} else {
			loading.cancelLoading();
			display.setCurrent(alert, next);
		}
		
		return alert;
	}
}
