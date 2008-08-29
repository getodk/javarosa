package org.javarosa.demo.midlet;

import java.util.Stack;
import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.JavaRosaServiceProvider;
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
		/*
		 * Duplicate this class and change the following line to
		 * create a custom midlet to launch from
		 */
		shell = new JavaRosaDemoShell();

		// Do NOT edit below
		JavaRosaServiceProvider.instance().initialize();
		JavaRosaServiceProvider.instance().setDisplay(Display.getDisplay(this));
		((JavaRosaDemoShell)shell).setMIDlet(this);
		shell.run();
	}

}
