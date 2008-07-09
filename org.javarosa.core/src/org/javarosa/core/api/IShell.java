package org.javarosa.core.api;

import java.util.Hashtable;

import javax.microedition.lcdui.Displayable;

public interface IShell {
	/**
	 * Called when this IShell should start to run
	 */
	void run();
	
	/**
	 * Called when a module has completed running and should return control here.
	 */
	void returnFromModule(IModule module, String returnCode, Hashtable returnArgs);

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
	void setDisplay(IModule callingModule, Displayable display);
}
