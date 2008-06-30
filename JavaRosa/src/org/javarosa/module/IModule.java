/**
 * 
 */
package org.javarosa.module;

import org.javarosa.view.ReturnValue;

/**
 * @author Brian DeRenzi
 *
 */
public interface IModule {
	/**
	 * added Jon's method from IViewController
	 * @param args
	 * @param viewId
	 */
	public void viewCompleted(ReturnValue rv, int viewId);
	
	public void start();
}
