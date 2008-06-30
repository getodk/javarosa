package org.javarosa.shell;

import java.util.Hashtable;

import org.javarosa.module.IModule;

public interface IShell {
	/**
	 * Called when this IShell should start to run
	 */
	void Run();

	/**
	 * Called when a module has completed running and should return control here.
	 */
	void moduleCompleted(IModule module, Hashtable context);

	/**
	 * Called when this IShell is being exited.  This could be another application loading of this application quitting.
	 */
	void ExitShell();

	
}
