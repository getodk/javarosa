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

package org.javarosa.shellformtest.midlet;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IShell;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.shellformtest.shell.JavaRosaFormTestShell;

/**
 * This is the starting point for the JavarosaDemo application
 * @author Brian DeRenzi
 *
 */
public class JavaRosaFormTestMidlet extends MIDlet {
	IShell shell = null;
	
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		/*
		 * Duplicate this class and change the following line to 
		 * create a custom midlet to launch from 
		 */
		shell = new JavaRosaFormTestShell();

		// Do NOT edit below
		JavaRosaServiceProvider.instance().initialize();
		JavaRosaServiceProvider.instance().setDisplay(new J2MEDisplay(Display.getDisplay(this)));
		shell.run();
		((JavaRosaFormTestShell)shell).setMIDlet(this);
	}

}
