package org.javarosa.demo.midlet;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IDisplay;
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
	private IShell shell;

	protected void startApp() throws MIDletStateChangeException {
		/*
		 * Duplicate this class and change the following line to create a custom
		 * midlet to launch from
		 */
		this.shell = new JavaRosaDemoShell(this);

		// Initialize the service provider (TODO: though currently does nothing?)
		JavaRosaServiceProvider.instance().initialize();

		// Set a display object in the service provider
		// A display controls views
		IDisplay display = new J2MEDisplay(Display.getDisplay(this));
		JavaRosaServiceProvider.instance().setDisplay(display);

		// launch the shell
		this.shell.run();
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// do nothing
	}

	protected void pauseApp() {
		// do nothing
	}

}
