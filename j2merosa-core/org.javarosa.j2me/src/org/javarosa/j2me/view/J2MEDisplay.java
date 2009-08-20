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
import javax.microedition.midlet.MIDlet;

public class J2MEDisplay {
	private static Display display;
	
	public static void init (MIDlet m) {
		setDisplay(Display.getDisplay(m));
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
	
	public void setView (Displayable d) {
		display.setCurrent(d);
	}
	
	
//    //good utility function
//    private void showError(String title, String message) {
//    	//#style mailAlert
//    	Alert alert = new Alert(title, message, null, AlertType.ERROR);
//    	alert.setTimeout(Alert.FOREVER);
//    	//alert.setCommandListener(this);
//    	
//    	// Clayton Sims - Apr 27, 2009 : 
//    	//Really not-thrilled with how this works. Unfortunately this is an instance of views not being able
//    	//to propogate up new views. Maybe there should be a centralized way to use alerts? Or are we fine
//    	//with using the existing elements?
//    	Alert.setCurrent((Display)JavaRosaServiceProvider.instance().getDisplay().getDisplayObject(), alert, this);
//    }
//    
//    
//	private void showError(String s, String message) {
//		showError(s, message, this);
//	}
//
//	private void showError(String s, String message, CommandListener cl) {
//		//#style mailAlert
//		final Alert alert = new Alert(s, message, null, AlertType.ERROR);
//		alert.setTimeout(Alert.FOREVER);
//		if (cl != null) {
//			//alert.setCommandListener(cl);
//		}
//		J2MEDisplay.getDisplay().setCurrent(alert, view);
//	}
	
}
