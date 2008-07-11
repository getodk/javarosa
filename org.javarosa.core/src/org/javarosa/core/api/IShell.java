package org.javarosa.core.api;

import java.util.Hashtable;

import javax.microedition.lcdui.Displayable;

/**
 * Shells are responsible for controlling the workflow of a 
 * JavaRosa application. It is responsibly for spawning activities,
 * managing their returns, and mitigating access to the Application's 
 * display. 
 * 
 * @author Clayton Sims
 *
 */
public interface IShell {
	/**
	 * Called when this IShell should start to run
	 */
	void run();
	
	/**
	 * Called when a module has completed running and should return control here.
	 */
	void returnFromModule(IActivity activity, String returnCode, Hashtable returnArgs);

	/**
	 * Called when this IShell is being exited.  This could be another application loading of this application quitting.
	 */
	void exitShell();

	/**
	 * Sets the current display, taking into account what module is currently executing 
	 * 
	 * @param callingModule The module attempting to set the displayable
	 * @param display The displayable to be set
	 */
	void setDisplay(IActivity callingActivity, Displayable display);
}
