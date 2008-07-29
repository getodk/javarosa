package org.javarosa.action;


/**
 * Represents a user interface action.
 * This differs from an activity by not requiring to be launched by the shell and
 * by an action being able to launch as many other actions as it wants.
 * 
 * @author daniel
 *
 */
public interface IAction {
	
	/**
	 * Starts an action.
	 * 
	 * @param listener the listener for action commands.
	 */
	public void start(IActionListener listener);
	
	/**
	 * Continues the action from where it had stopped, with all the 
	 * preserved state (e.g last selected item)
	 *
	 */
	public void resume();
}
