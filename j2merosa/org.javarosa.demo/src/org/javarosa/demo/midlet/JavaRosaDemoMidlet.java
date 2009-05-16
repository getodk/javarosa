package org.javarosa.demo.midlet;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IShell;
import org.javarosa.demo.shell.JavaRosaDemoShell;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * This is the starting point for the JavarosaDemo application
 * 
 * @author Brian DeRenzi
 * 
 */
public class JavaRosaDemoMidlet extends MIDlet {
	private IShell shell = null;

	protected void startApp() throws MIDletStateChangeException {
		/*
		 * Duplicate this class and change the following line to create a custom
		 * midlet to launch from
		 */
		shell = new JavaRosaDemoShell(this);

		// Do NOT edit below
		JavaRosaServiceProvider.instance().initialize();
		JavaRosaServiceProvider.instance().setDisplay(
				new J2MEDisplay(Display.getDisplay(this)));
		shell.run();
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// do nothing
	}

	protected void pauseApp() {
		// do nothing
	}

}
