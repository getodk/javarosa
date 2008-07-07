package org.javarosa.demo.midlet;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.JavaRosaPlatform;
import org.javarosa.core.api.IShell;
import org.javarosa.demo.shell.JavaRosaDemoShell;

/**
 * This is the starting point for the JavarosaDemo application
 * @author Brian DeRenzi
 *
 */
public class JavaRosaDemoMidlet extends MIDlet {
	IShell shell = null;
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		// TODO Auto-generated method stub
		
		/*
		 * Duplicate this class and change the following line to 
		 * create a custom midlet to launch from 
		 */
		shell = new JavaRosaDemoShell();

		// Do NOT edit below
		JavaRosaPlatform.instance().initialize();
		JavaRosaPlatform.instance().setDisplay(Display.getDisplay(this));
		shell.run();
	}

}
