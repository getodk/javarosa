package org.javarosa.core.api;

import org.javarosa.core.Context;

/**
 * The Module Interface represents an integration point
 * for an extensible set of JavaRosa code. A Module is
 * used to configure a set of components with any application
 * which might use them.
 *  
 * @author Clayton Sims
 *
 */
public interface IModule {
	/**
	 * Register Module should identify all configuration that
	 * needs to occur for the elements that are contained within
	 * a module, and perform that configuration and registration
	 * with the current application.
	 */
	public void registerModule(Context context);
}
